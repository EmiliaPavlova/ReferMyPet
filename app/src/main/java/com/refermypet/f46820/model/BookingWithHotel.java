package com.refermypet.f46820.model;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.List;

public class BookingWithHotel {
    @Embedded
    public Booking booking;

    @Relation(
            parentColumn = "hotel_fk_id",
            entityColumn = "hotel_id"
    )
    public Hotel hotel;

    @Relation(
            parentColumn = "person_fk_id",
            entityColumn = "person_id"
    )
    public Person person;

    @Relation(
            parentColumn = "booking_id",
            entityColumn = "booking_fk_id"
    )
    public List<Referral> referrals;

    @Ignore
    public List<Pet> getPets() {
        return booking != null ? booking.selectedPets : null;
    }

    @Ignore
    public boolean hasReview;
}