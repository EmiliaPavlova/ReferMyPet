package com.refermypet.f46820.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

import com.refermypet.f46820.enums.PetType;

@Entity(tableName = "pets",
        foreignKeys = @ForeignKey(entity = Person.class,
                parentColumns = "person_id",
                childColumns = "owner_fk_id",
                onDelete = CASCADE))
public class Pet {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pet_id")
    public int id;

    @ColumnInfo(name = "owner_fk_id", index = true) // FK към RegularUsers таблицата
    public int ownerId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "breed")
    public String breed;

    @ColumnInfo(name = "type")
    public PetType type;

    @ColumnInfo(name = "birth_date")
    public String birthDate;

    @ColumnInfo(name = "chip_number")
    public String chipNumber;

    @ColumnInfo(name = "rating")
    public float rating;

    @ColumnInfo(name = "photo_path")
    public String photoPath;

    public Pet(int ownerId, String name, String breed, PetType type, String birthDate, String chipNumber, String photoPath, float rating) {
        this.ownerId = ownerId;
        this.name = name;
        this.breed = breed;
        this.type = type;
        this.birthDate = birthDate;
        this.chipNumber = chipNumber;
        this.photoPath = photoPath;
        this.rating = rating;
    }

    public PetType getType() {
        return type;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getChipNumber() {
        return chipNumber;
    }

    public float getRating() {
        return rating;
    }
}
