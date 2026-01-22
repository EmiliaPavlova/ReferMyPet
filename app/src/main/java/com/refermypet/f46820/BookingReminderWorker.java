package com.refermypet.f46820;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingReminderWorker extends Worker {
    private static final String CHANNEL_ID = "BookingChannel";
    private static final int NOTIFICATION_ID = 101;

    public BookingReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String endDate = getInputData().getString("end_date");

        if (isBookingExpired(endDate)) {
            return Result.success();
        }

        createNotificationChannel();
        showNotification(endDate);

        return Result.success();
    }

    private void showNotification(String endDate) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        Intent contentIntent = new Intent(context, HomeActivity.class);
        contentIntent.putExtra("end_date", endDate);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default_pet_placeholder))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true);

        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private boolean isBookingExpired(String endDate) {
        if (endDate == null || endDate.isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date end = sdf.parse(endDate);
            return new Date().after(end);
        } catch (Exception e) {
            return false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Booking Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    // Show notifications at 14:00.
    public static void scheduleReminder(Context context, String endDate) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long delay = calendar.getTimeInMillis() - now;

        Data data = new Data.Builder()
                .putString("end_date", endDate)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BookingReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("booking_reminder")
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }
}