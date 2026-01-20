package com.refermypet.f46820.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.refermypet.f46820.model.Hotel;

import java.util.List;

@Dao
public interface HotelDao {
    @Insert
    long insert(Hotel hotel);

    @Query("SELECT * FROM hotels WHERE user_fk_id = :userId LIMIT 1")
    Hotel getHotelByUserId(int userId);

    @Query("SELECT * FROM hotels")
    List<Hotel> getAllHotels();

    @Update
    void update(Hotel hotel);
}
