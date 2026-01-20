package com.refermypet.f46820.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import static androidx.room.ForeignKey.CASCADE;

import java.util.List;

/**
 * Entity representing a pet boarding reservation.
 * Links a pet to a hotel for a specific period.
 */
@Entity(tableName = "bookings",
        foreignKeys = {
                @ForeignKey(entity = Person.class, parentColumns = "person_id", childColumns = "person_fk_id", onDelete = CASCADE),
                @ForeignKey(entity = Hotel.class, parentColumns = "hotel_id", childColumns = "hotel_fk_id", onDelete = CASCADE)
        })
public class Booking {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "booking_id")
    public int id;

    @ColumnInfo(name = "person_fk_id", index = true)
    public int personId;

    @ColumnInfo(name = "hotel_fk_id", index = true)
    public int hotelId;

    @ColumnInfo(name = "start_date")
    public String startDate;

    @ColumnInfo(name = "end_date")
    public String endDate;

    @ColumnInfo(name = "selected_pets_names")
    public List<Pet> selectedPets;

    public Booking(int personId, int hotelId, String startDate, String endDate, List<Pet> selectedPets) {
        this.personId = personId;
        this.hotelId = hotelId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedPets = selectedPets;
    }
}