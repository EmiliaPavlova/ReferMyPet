package com.refermypet.f46820.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.refermypet.f46820.AppDatabase;
import com.refermypet.f46820.BookingReminderWorker;
import com.refermypet.f46820.enums.UserType;
import com.refermypet.f46820.model.Booking;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.model.Person;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.model.Referral;
import com.refermypet.f46820.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel responsible for managing dashboard data for both Persons and Hotels.
 */
public class UserViewModel extends AndroidViewModel {

    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<List<BookingWithHotel>> bookingsList = new MutableLiveData<>();
    private final MutableLiveData<List<BookingWithHotel>> allBookingsList = new MutableLiveData<>();
    private final MutableLiveData<List<BookingWithHotel>> pastBookingsList = new MutableLiveData<>();
    private final MutableLiveData<BookingWithHotel> selectedBooking = new MutableLiveData<>();
    private final MutableLiveData<Referral> latestReferral = new MutableLiveData<>();
    private final MutableLiveData<UserType> userType = new MutableLiveData<>();
    private final MutableLiveData<List<Hotel>> allHotels = new MutableLiveData<>();
    private final MutableLiveData<Person> currentPerson = new MutableLiveData<>();
    private final MutableLiveData<Hotel> currentHotel = new MutableLiveData<>();
    private final MutableLiveData<List<Pet>> userPets = new MutableLiveData<>();
    private final MutableLiveData<String> passwordStatus = new MutableLiveData<>();
    public LiveData<String> getPasswordStatus() { return passwordStatus; }

    private final MutableLiveData<Float> ownerAverageRating = new MutableLiveData<>();

    private int currentPersonId;
    private int currentUserId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AppDatabase db;

    public UserViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
    }

    public LiveData<String> getUserName() { return userName; }
    public LiveData<List<BookingWithHotel>> getBookingsList() { return bookingsList; }
    public LiveData<List<BookingWithHotel>> getAllBookingsList() { return allBookingsList; }
    public LiveData<List<BookingWithHotel>> getPastBookingsList() { return pastBookingsList; }
    public LiveData<BookingWithHotel> getSelectedBooking() { return selectedBooking; }
    public LiveData<Referral> getLatestReferral() { return latestReferral; }
    public LiveData<UserType> getUserType() { return userType; }
    public LiveData<List<Hotel>> getAllHotelsList() { return allHotels; }
    public int getCurrentPersonId() { return currentPersonId; }
    public LiveData<Person> getCurrentPerson() { return currentPerson; }
    public LiveData<Hotel> getCurrentHotel() { return currentHotel; }
    public LiveData<List<Pet>> getUserPets() { return userPets; }
    public LiveData<Float> getOwnerAverageRating() { return ownerAverageRating; }

    /**
     * Returns the currently memorized user ID.
     */
    public int getCurrentUserId() { return currentUserId; }

    /**
     * Loads all hotels from the database and posts them to the LiveData.
     */
    public void loadAllHotels() {
        executorService.execute(() -> {
            List<Hotel> hotels = db.hotelDao().getAllHotels();
            allHotels.postValue(hotels);
        });
    }

    /**
     * Inserts a new booking into the database.
     */
    public void addBooking(int personId, int hotelId, String startDate, String endDate, List<Pet> selectedPets) {
        executorService.execute(() -> {
            Booking newBooking = new Booking(personId, hotelId, startDate, endDate, selectedPets);
            db.bookingDao().insert(newBooking);
            BookingReminderWorker.scheduleReminder(getApplication(), endDate);
            loadDashboardData(currentUserId);
        });
    }

    /**
     * Loads all bookings
     * @param userId
     */
    public void loadAllBookings(int userId) {
        executorService.execute(() -> {
            Person person = db.personDao().getPersonByUserId(userId);
            if (person != null) {
                List<BookingWithHotel> all = db.bookingDao().getAllBookingsForPersonSync(person.getId());
                allBookingsList.postValue(all);

                for (BookingWithHotel b : all) {
                    if (b.booking != null) {
                        BookingReminderWorker.scheduleReminder(getApplication(), b.booking.endDate);
                    }
                }
            }
        });
    }

    /**
     * Loads past bookings for a Hotel where the end date has passed.
     * @param userId The ID of the user (to find the hotel).
     */
    public void loadPastBookingsForHotel(int userId) {
        executorService.execute(() -> {
            Hotel hotel = db.hotelDao().getHotelByUserId(userId);
            if (hotel != null) {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                List<BookingWithHotel> past = db.bookingDao().getPastBookingsForHotelSync(hotel.getId(), currentDate);

                if (past != null) {
                    for (BookingWithHotel b : past) {
                        if (b.booking != null) {
                            boolean exists = (b.referrals != null && !b.referrals.isEmpty());
                            if (!exists) {
                                exists = db.bookingDao().getReviewCountForBooking(b.booking.id) > 0;
                            }
                            b.hasReview = exists;
                        }
                    }
                }
                pastBookingsList.postValue(past);
            }
        });
    }

    /**
     * Saves a new referral for a guest.
     */
    public void addReview(BookingWithHotel item, float rating, String text) {
        executorService.execute(() -> {
            List<Pet> pets = item.getPets();
            if (pets != null) {
                for (Pet pet : pets) {
                    Referral newReferral = new Referral(
                            item.booking.id,
                            pet.id,
                            item.hotel.id,
                            rating,
                            text,
                            item.booking.endDate
                    );
                    db.referralDao().insert(newReferral);
                }
            }
            loadAllBookings(currentUserId);
            loadPastBookingsForHotel(currentUserId);
        });
    }

    /**
     * Loads a specific booking by its ID
     * @param bookingId
     */
    public void loadBookingById(int bookingId) {
        executorService.execute(() -> {
            BookingWithHotel booking = db.bookingDao().getBookingById(bookingId);
            selectedBooking.postValue(booking);
        });
    }

    /**
     * Deletes a booking from the database.
     * @param booking
     */
    public void deleteBooking(com.refermypet.f46820.model.Booking booking) {
        executorService.execute(() -> {
            db.bookingDao().delete(booking);
            loadDashboardData(currentUserId);
        });
    }

    /**
     * Loads full profile data for the logged user.
     */
    public void loadUserProfile(int userId) {
        executorService.execute(() -> {
            User user = db.userDao().findById(userId);
            if (user == null) return;

            if (user.getUserType() == UserType.PERSON) {
                Person person = db.personDao().getPersonByUserId(userId);
                if (person != null) {
                    currentPerson.postValue(person);
                    List<Pet> pets = db.petDao().getPetsByOwnerId(person.getId());
                    userPets.postValue(pets);
                }
            } else if (user.getUserType() == UserType.HOTEL) {
                Hotel hotel = db.hotelDao().getHotelByUserId(userId);
                if (hotel != null) {
                    currentHotel.postValue(hotel);
                }
            }
        });
    }

    /**
     * Updates the Person details in the database.
     */
    public void updatePerson(Person person) {
        executorService.execute(() -> {
            db.personDao().update(person);
            currentPerson.postValue(person);
        });
    }

    /**
     * Updates the Hotel details in the database.
     */
    public void updateHotel(Hotel hotel) {
        executorService.execute(() -> {
            db.hotelDao().update(hotel);
            currentHotel.postValue(hotel);
        });
    }

    /**
     * Updates the pets list by deleting old records and inserting updated ones.
     */
    public void updatePets(int ownerId, List<Pet> newPets) {
        executorService.execute(() -> {
            db.petDao().deleteByOwnerId(ownerId);
            for (Pet pet : newPets) {
                db.petDao().insert(pet);
            }
            userPets.postValue(newPets);
        });
    }

    /**
     * Updates the user's password in the database.
     */
    public void updateUserPassword(int userId, String oldPassword, String newPassword) {
        executorService.execute(() -> {
            User user = db.userDao().findById(userId);
            if (user != null) {
                String hashedOldInput = hashPassword(oldPassword);

                if (user.passwordHash.equals(hashedOldInput)) {
                    user.passwordHash = hashPassword(newPassword);
                    db.userDao().update(user);
                    passwordStatus.postValue("SUCCESS");
                } else {
                    passwordStatus.postValue("WRONG_OLD_PASSWORD");
                }
            }
        });
    }

    /**
     * Standard SHA-256 hashing to match your registration logic.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    /**
     * Fetches and posts dashboard data based on the user's role.
     * @param userId The ID of the logged-in user.
     */
    public void loadDashboardData(int userId) {
        this.currentUserId = userId;

        executorService.execute(() -> {
            try {
                User user = db.userDao().findById(userId);
                if (user == null) return;

                userType.postValue(user.getUserType());
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                if (user.getUserType() == UserType.PERSON) {
                    Person person = db.personDao().getPersonByUserId(userId);
                    if (person != null) {
                        currentPersonId = person.getId();
                        userName.postValue(person.getFirstName());
                        bookingsList.postValue(db.bookingDao().getBookingsForPersonSync(person.getId(), currentDate));
                        latestReferral.postValue(db.referralDao().getLatestReferralForOwner(person.getId()).getValue());

                        db.referralDao().getAverageRatingForOwner(person.getId()).observeForever(avg -> {
                            if (avg != null) ownerAverageRating.postValue(avg);
                        });

                        db.referralDao().getLatestReferralForOwner(person.getId()).observeForever(ref -> {
                            if (ref != null) latestReferral.postValue(ref);
                        });
                    }
                } else if (user.getUserType() == UserType.HOTEL) {
                    Hotel hotel = db.hotelDao().getHotelByUserId(userId);
                    if (hotel != null) {
                        currentHotel.postValue(hotel);
                        userName.postValue(hotel.getName());
                        bookingsList.postValue(db.bookingDao().getBookingsForHotelSync(hotel.getId(), currentDate));
                        latestReferral.postValue(db.referralDao().getLatestReferralByHotel(hotel.getId()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Calculates the average rating using the 'allBookingsList' variable.
     */
    public float getCalculatedAverageRating() {
        List<BookingWithHotel> bookings = allBookingsList.getValue();
        if (bookings == null || bookings.isEmpty()) return 0.0f;

        float totalScore = 0;
        int count = 0;

        for (BookingWithHotel item : bookings) {
            if (item.referrals != null && !item.referrals.isEmpty()) {
                for (com.refermypet.f46820.model.Referral ref : item.referrals) {
                    totalScore += ref.getRatingScore();
                    count++;
                }
            }
        }
        return count > 0 ? totalScore / count : 0.0f;
    }

    /**
     * Gets the latest referral text using the 'allBookingsList' variable.
     */
    public String getLatestReferralText() {
        List<BookingWithHotel> bookings = allBookingsList.getValue();
        if (bookings == null || bookings.isEmpty()) return null;

        for (BookingWithHotel item : bookings) {
            if (item.referrals != null && !item.referrals.isEmpty()) {
                return item.referrals.get(0).getRecommendationText();
            }
        }
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}