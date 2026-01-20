package com.refermypet.f46820.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.refermypet.f46820.model.Person;
@Dao
public interface PersonDao {
    @Insert
    long insert(Person person);

    @Query("SELECT * FROM persons WHERE user_fk_id = :userId LIMIT 1")
    Person getPersonByUserId(int userId);

    @Update
    void update(Person person);
}
