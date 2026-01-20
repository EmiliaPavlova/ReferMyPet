package com.refermypet.f46820.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import com.refermypet.f46820.model.Photo;

@Dao
public interface PhotoDao {
    @Insert
    long insert(Photo photo);
}
