package com.refermypet.f46820.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "referrals",
        foreignKeys = {
                @ForeignKey(entity = Pet.class, parentColumns = "pet_id", childColumns = "pet_fk_id", onDelete = CASCADE),
                @ForeignKey(entity = Hotel.class, parentColumns = "hotel_id", childColumns = "hotel_fk_id", onDelete = CASCADE),
                @ForeignKey(entity = Booking.class, parentColumns = "booking_id", childColumns = "booking_fk_id", onDelete = CASCADE)
        })
public class Referral {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "referral_id")
    public int id;

    @ColumnInfo(name = "booking_fk_id", index = true)
    public int bookingId;

    @ColumnInfo(name = "pet_fk_id", index = true)
    public int petId;

    @ColumnInfo(name = "hotel_fk_id", index = true)
    public int hotelId;

    @ColumnInfo(name = "rating_score")
    public float ratingScore;

    @ColumnInfo(name = "recommendation_text")
    public String recommendationText;

    @ColumnInfo(name = "date_of_stay")
    public String dateOfStay;

    public Referral(int bookingId, int petId, int hotelId, float ratingScore, String recommendationText, String dateOfStay) {
        this.bookingId = bookingId;
        this.petId = petId;
        this.hotelId = hotelId;
        this.ratingScore = ratingScore;
        this.recommendationText = recommendationText;
        this.dateOfStay = dateOfStay;
    }

    public int getId() {
        return id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getPetId() {
        return petId;
    }

    public int getHotelId() {
        return hotelId;
    }

    public float getRatingScore() {
        return ratingScore;
    }

    public String getRecommendationText() {
        return recommendationText;
    }

    public String getDateOfStay() {
        return dateOfStay;
    }
}
