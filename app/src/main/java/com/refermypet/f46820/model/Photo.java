package com.refermypet.f46820.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "photos",
        foreignKeys = {
                @ForeignKey(entity = Hotel.class, parentColumns = "hotel_id", childColumns = "hotel_fk_id"),
                @ForeignKey(entity = Pet.class, parentColumns = "pet_id", childColumns = "pet_fk_id")
        })
public class Photo {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "photo_id")
    public int id;

    @ColumnInfo(name = "hotel_fk_id", index = true)
    public Integer hotelId;

    @ColumnInfo(name = "pet_fk_id", index = true)
    public Integer petId;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    public int getId() {
        return id;
    }
}
