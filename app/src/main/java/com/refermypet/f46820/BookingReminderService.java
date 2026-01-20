package com.refermypet.f46820;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Service to handle background notifications and rescheduling for bookings.
 */
public class BookingReminderService extends Service {
    private static final String CHANNEL_ID = "BookingChannel";
    private static final int NOTIFICATION_ID = 101;

    public static final String ACTION_STOP = "com.refermypet.f46820.ACTION_STOP";
    public static final String ACTION_SNOOZE = "com.refermypet.f46820.ACTION_SNOOZE";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        String endDate = intent.getStringExtra("end_date");

        // 1. Handle STOP action from notification
        if (ACTION_STOP.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // 2. Handle SNOOZE action (Schedule for tomorrow and stop now)
        if (ACTION_SNOOZE.equals(action)) {
            cancelNotification();
            scheduleNextDay(endDate);
            stopSelf();
            return START_NOT_STICKY;
        }

        // 3. Validation: Stop service if the booking has expired
        if (isBookingExpired(endDate)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // 4. Default behavior: Show the notification
        showNotification(endDate);

        // Ensures service is restarted by system if killed
        return START_STICKY;
    }

    private void showNotification(String endDate) {
        // PendingIntent flags for compatibility (Required for Android 12+)
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        int userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("USER_ID", -1);

        // Intent to open the main app when clicking the notification body
        Intent contentIntent = new Intent(this, HomeActivity.class);
        contentIntent.putExtra("end_date", endDate);
        contentIntent.putExtra("USER_ID", userId);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, pendingFlags);

        // Snooze button intent (re-triggers service with ACTION_SNOOZE)
        Intent snoozeIntent = new Intent(this, BookingReminderService.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra("end_date", endDate);
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, 2, snoozeIntent, pendingFlags);

        // Stop button intent
        Intent stopIntent = new Intent(this, BookingReminderService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_pet_placeholder))
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_lock_idle_alarm, getString(R.string.action_snooze), snoozePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_stop), stopPendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void scheduleNextDay(String endDate) {
        Intent intent = new Intent(this, BookingReminderService.class);
        intent.putExtra("end_date", endDate);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, flags);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // TODO: change with workmanager, add time for the notification (14:00)
        if (alarmManager != null) {
            // Set for 24 hours (1 day) from now
            long triggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
            Toast.makeText(this, "Reminder snoozed for 24h", Toast.LENGTH_SHORT).show();
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

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Booking Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}