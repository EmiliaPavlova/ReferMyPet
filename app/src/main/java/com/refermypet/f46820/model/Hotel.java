package com.refermypet.f46820.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "hotels",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_fk_id",
                onDelete = CASCADE))
public class Hotel {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "hotel_id")
    public int id;

    @ColumnInfo(name = "user_fk_id", index = true)
    public int userId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "chain")
    public String chain;

    @ColumnInfo(name = "country")
    public String country;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "postal_code")
    public String postalCode;

    @ColumnInfo(name = "street_address")
    public String streetAddress;

    @ColumnInfo(name = "phone_primary")
    public String phonePrimary;

    @ColumnInfo(name = "phone_secondary")
    public String phoneSecondary;

    @ColumnInfo(name = "description")
    public String description;

    public Hotel(int userId, String name, String chain, String country, String city, String postalCode, String streetAddress, String phonePrimary, String phoneSecondary, String description) {
        this.userId = userId;
        this.name = name;
        this.chain = chain;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.streetAddress = streetAddress;
        this.phonePrimary = phonePrimary;
        this.phoneSecondary = phoneSecondary;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getChain() {
        return chain;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPhonePrimary() {
        return phonePrimary;
    }

    public String getPhoneSecondary() {
        return phoneSecondary;
    }

    public String getDescription() {
        return description;
    }
}