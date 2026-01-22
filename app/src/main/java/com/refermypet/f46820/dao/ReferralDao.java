package com.refermypet.f46820.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.refermypet.f46820.model.Referral;

@Dao
public interface ReferralDao {
    @Insert
    void insert(Referral referral);

    /**
     * Finds the latest referral for any pet owned by the specific user.
     * Join with the 'pets' table to find the connection.
     */
    @Query("SELECT referrals.* FROM referrals " +
            "INNER JOIN pets ON referrals.pet_fk_id = pets.pet_id " +
            "WHERE pets.owner_fk_id = :personId " +
            "ORDER BY referrals.referral_id DESC LIMIT 1")
    Referral getLatestReferralByUserId(int personId);

    /**
     * Finds the latest referral for a specific hotel.
     */
    @Query("SELECT * FROM referrals WHERE hotel_fk_id = :hotelId ORDER BY referral_id DESC LIMIT 1")
    Referral getLatestReferralByHotel(int hotelId);

    /**
     * Returns the average rating for a pet's owner.
     */
    @Query("SELECT AVG(r.rating_score) FROM referrals r " +
            "JOIN pets p ON r.pet_fk_id = p.pet_id " +
            "WHERE p.owner_fk_id = :ownerId")
    LiveData<Float> getAverageRatingForOwner(int ownerId);

    /**
     * Returns the latest referral for a specific owner.
     */
    @Query("SELECT r.* FROM referrals r " +
            "JOIN pets p ON r.pet_fk_id = p.pet_id " +
            "WHERE p.owner_fk_id = :ownerId " +
            "ORDER BY r.referral_id DESC LIMIT 1")
    LiveData<Referral> getLatestReferralForOwner(int ownerId);
}