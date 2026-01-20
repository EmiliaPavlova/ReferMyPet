package com.refermypet.f46820.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.refermypet.f46820.model.Pet;
import java.util.List;

@Dao
public interface PetDao {
    @Insert
    long insert(Pet pet);

    @Query("SELECT COUNT(pet_id) FROM pets WHERE owner_fk_id = :ownerId")
    int countPetsByOwner(int ownerId);

    @Query("SELECT * FROM pets WHERE owner_fk_id = :ownerId")
    List<Pet> getPetsByOwnerId(int ownerId);

    @Query("DELETE FROM pets WHERE owner_fk_id = :ownerId")
    void deleteByOwnerId(int ownerId);
}
