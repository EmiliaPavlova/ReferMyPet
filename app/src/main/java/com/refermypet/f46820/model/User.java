package com.refermypet.f46820.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.refermypet.f46820.enums.UserType;

/**
 * Represents the core User entity for authentication and type identification.
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public int id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "user_type") // a PERSON or a HOTEL
    public UserType userType;

    public User(String email, String passwordHash, UserType userType) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public UserType getUserType() {
        return userType;
    }
}
