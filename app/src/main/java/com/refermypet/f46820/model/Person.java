package com.refermypet.f46820.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "persons",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_fk_id",
                onDelete = CASCADE))
public class Person {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "person_id")
    public int id;

    @ColumnInfo(name = "user_fk_id", index = true)
    public int userId;

    @NonNull
    @ColumnInfo(name = "first_name")
    public String firstName;

    @NonNull
    @ColumnInfo(name = "last_name")
    public String lastName;

    @NonNull
    @ColumnInfo(name = "birth_date")
    public String birthDate;

    @ColumnInfo(name = "country")
    public String country;

    @ColumnInfo(name = "city")
    public String city;

    @ColumnInfo(name = "street_address")
    public String streetAddress;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "profile_photo_path")
    public String profilePhotoPath;

    public Person(int userId, @NonNull String firstName, @NonNull String lastName, @NonNull String birthDate, String country, String city, String streetAddress, String phone, String profilePhotoPath) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
        this.city = city;
        this.streetAddress = streetAddress;
        this.phone = phone;
        this.profilePhotoPath = profilePhotoPath;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }
}
