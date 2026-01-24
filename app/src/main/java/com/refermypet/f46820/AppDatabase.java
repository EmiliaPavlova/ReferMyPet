package com.refermypet.f46820;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.refermypet.f46820.dao.BookingDao;
import com.refermypet.f46820.dao.UserDao;
import com.refermypet.f46820.dao.HotelDao;
import com.refermypet.f46820.dao.PersonDao;
import com.refermypet.f46820.dao.PetDao;
import com.refermypet.f46820.dao.ReferralDao;
import com.refermypet.f46820.model.Booking;
import com.refermypet.f46820.model.User;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.model.Person;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.model.Referral;
import com.refermypet.f46820.utils.Converters;

/**
 * Main Database configuration using Room.
 * Defines entities, versioning, and provides access to DAOs.
 */
@Database(entities = {
        User.class,
        Hotel.class,
        Person.class,
        Pet.class,
        Referral.class,
        Booking.class
},
        version = 1,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract HotelDao hotelDao();
    public abstract PersonDao personDao();
    public abstract BookingDao bookingDao();
    public abstract ReferralDao referralDao();
    public abstract PetDao petDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Singleton pattern to ensure only one database instance is used throughout the app.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "pet_referral_db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
