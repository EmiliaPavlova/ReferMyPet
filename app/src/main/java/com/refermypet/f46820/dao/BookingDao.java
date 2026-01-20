package com.refermypet.f46820.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.refermypet.f46820.model.Booking;
import com.refermypet.f46820.model.BookingWithHotel;
import java.util.List;

@Dao
public interface BookingDao {
    @Insert
    void insert(Booking booking);

    /**
     * Fetches all upcoming reservations for a specific person.
     * Ordered by ID descending to show the newest first.
     */
    @Transaction
    @Query("SELECT * FROM bookings WHERE person_fk_id = :personId AND end_date >= :today ORDER BY start_date ASC")
    List<BookingWithHotel> getBookingsForPersonSync(int personId, String today);

    /**
     * Fetches all reservations for a specific person.
     * @param personId
     * @return List of bookings for a specific person
     */
    @Transaction
    @Query("SELECT * FROM bookings WHERE person_fk_id = :personId ORDER BY start_date DESC")
    List<BookingWithHotel> getAllBookingsForPersonSync(int personId);

    /**
     * Fetches reservations for a hotel that start on or before the current date.
     * Used for the Hotel Dashboard to show current and past bookings.
     */
    @Transaction
    @Query("SELECT * FROM bookings WHERE hotel_fk_id = :hotelId AND end_date >= :today ORDER BY start_date ASC")
    List<BookingWithHotel> getBookingsForHotelSync(int hotelId, String today);

    @Transaction
    @Query("SELECT * FROM bookings WHERE booking_id = :id")
    BookingWithHotel getBookingById(int id);

    @Transaction
    @Query("SELECT * FROM bookings WHERE hotel_fk_id = :hotelId AND end_date < :currentDate")
    List<BookingWithHotel> getPastBookingsForHotelSync(int hotelId, String currentDate);

    @Query("SELECT COUNT(*) FROM referrals WHERE booking_fk_id = :bookingId")
    int getReviewCountForBooking(int bookingId);

    @Delete
    void delete(Booking booking);
}