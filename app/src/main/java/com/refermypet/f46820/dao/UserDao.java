package com.refermypet.f46820.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.refermypet.f46820.model.User;

/**
 * Data Access Object for the User entity.
 * Provides methods for authentication and user retrieval.
 */
@Dao
public interface UserDao {

    /**
     * Inserts a new user into the database.
     * @param user The user object to be saved.
     * @return The row ID of the newly inserted user.
     */
    @Insert
    long insert(User user);

    /**
     * Finds a user by their email address. Used during login.
     * @param email User's email.
     * @return The User object if found, null otherwise.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    /**
     * Retrieves a user by their unique ID.
     * Necessary for fetching profile data once the user is authenticated.
     * @param userId The ID of the user.
     * @return The User object.
     */
    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    User findById(int userId);

    /**
     * Counts how many users exist with a specific email. Used for validation during registration.
     * @param email The email to check.
     * @return Count of users found.
     */
    @Query("SELECT COUNT(user_id) FROM users WHERE email = :email")
    int countUsersByEmail(String email);

    @Update
    void update(User user);
}
