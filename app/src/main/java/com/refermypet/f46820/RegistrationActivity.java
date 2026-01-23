package com.refermypet.f46820;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.refermypet.f46820.dao.HotelDao;
import com.refermypet.f46820.dao.PersonDao;
import com.refermypet.f46820.dao.UserDao;
import com.refermypet.f46820.enums.UserType;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.model.Person;
import com.refermypet.f46820.model.User;
import com.refermypet.f46820.utils.PasswordHasher;

import org.jspecify.annotations.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the multi-step registration process for both individual owners and hotels.
 */
public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirm;
    private TextInputEditText etPFName, etPLName, etPBirth, etPCountry, etPCity, etPStreet, etPPhone;
    private TextInputEditText etHName, etHChain, etHCountry, etHCity, etHPostal, etHStreet, etHPhoneP, etHPhoneS, etHDesc;
    private Spinner spinnerUserType;
    private View includePerson, includeHotel;
    private Button btnSubmit;
    private Button btnSearch;
    private UserDao userDao;
    private PersonDao personDao;
    private HotelDao hotelDao;
    private ProgressBar pbLoading;

    // Background executor to ensure DB writes do not block the UI thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static class TempHotel {
        String name, city, street, postcode, country, phone;

        @Override
        public String toString() {
            return name + " (" + city + ", " + country + ", " + street + ")";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_reg);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        AppDatabase db = AppDatabase.getDatabase(this);
        userDao = db.userDao();
        personDao = db.personDao();
        hotelDao = db.hotelDao();

        initViews();
        setupSpinner();
        setupDatePicker();

        btnSubmit.setOnClickListener(v -> handleRegistration());
    }

    private void setupDatePicker() {
        etPBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18); // The USER should be at least 18 y.o.
            long eighteenYearsAgo = calendar.getTimeInMillis();

            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.before(eighteenYearsAgo))
                    .build();

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Birth Date")
                    .setSelection(eighteenYearsAgo)
                    .setCalendarConstraints(constraints)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etPBirth.setText(sdf.format(new Date(selection)));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_reg_email);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirm = findViewById(R.id.et_reg_confirm);

        etPFName = findViewById(R.id.et_p_first_name);
        etPLName = findViewById(R.id.et_p_last_name);
        etPBirth = findViewById(R.id.et_p_birth_date);
        etPCountry = findViewById(R.id.et_p_country);
        etPCity = findViewById(R.id.et_p_city);
        etPStreet = findViewById(R.id.et_p_street);
        etPPhone = findViewById(R.id.et_p_phone);

        etHName = findViewById(R.id.et_h_name);
        etHChain = findViewById(R.id.et_h_chain);
        etHCountry = findViewById(R.id.et_h_country);
        etHCity = findViewById(R.id.et_h_city);
        etHPostal = findViewById(R.id.et_h_postal);
        etHStreet = findViewById(R.id.et_h_street);
        etHPhoneP = findViewById(R.id.et_h_phone_p);
        etHPhoneS = findViewById(R.id.et_h_phone_s);
        etHDesc = findViewById(R.id.et_h_desc);

        spinnerUserType = findViewById(R.id.spinner_user_type);
        includePerson = findViewById(R.id.include_person);
        includeHotel = findViewById(R.id.include_hotel);
        btnSubmit = findViewById(R.id.btn_reg_submit);
        pbLoading = findViewById(R.id.pb_loading);
        btnSearch = findViewById(R.id.btn_search_hotel);

        btnSearch.setOnClickListener(v -> {
            String name = etHName.getText().toString().trim();
            searchHotelOnline(name);
        });
    }

    private void searchHotelOnline(String hotelName) {
        if (TextUtils.isEmpty(hotelName)) {
            Toast.makeText(this, getString(R.string.enter_hotel_name), Toast.LENGTH_SHORT).show();
            return;
        }

        // Main Thread
        pbLoading.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(false);

        executorService.execute(() -> {
            try {
                // All hotels that contain the name
                String urlString = "https://overpass-api.de/api/interpreter?data=[out:json];node[name~\"" + hotelName + "\",i][tourism=hotel];out;";
                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

                // adding header
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(5000); // 5 seconds timeout

                java.util.Scanner sc = new java.util.Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) sb.append(sc.nextLine());
                sc.close();

                org.json.JSONObject json = new org.json.JSONObject(sb.toString());
                org.json.JSONArray elements = json.getJSONArray("elements");

                java.util.List<TempHotel> foundHotels = new java.util.ArrayList<>();

                for (int i = 0; i < elements.length(); i++) {
                    org.json.JSONObject tags = elements.getJSONObject(i).getJSONObject("tags");
                    TempHotel temp = new TempHotel();
                    temp.name = tags.optString("name", "Unknown Hotel");
                    temp.city = tags.optString("addr:city", "N/A");
                    temp.country = tags.optString("addr:country", "N/A");
                    temp.street = tags.optString("addr:street", "") + " " + tags.optString("addr:housenumber", "");
                    temp.postcode = tags.optString("addr:postcode", "");
                    temp.phone = tags.optString("phone", "");
                    foundHotels.add(temp);
                }

                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE); // hide loader
                    btnSearch.setEnabled(true);
                    if (!foundHotels.isEmpty()) {
                        showHotelSelectionDialog(foundHotels);
                    } else {
                        Toast.makeText(this, getString(R.string.no_hotels_found), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    btnSearch.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_search_failed), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showHotelSelectionDialog(java.util.List<TempHotel> hotels) {
        String[] displayNames = new String[hotels.size()];
        for (int i = 0; i < hotels.size(); i++) {
            displayNames[i] = hotels.get(i).toString();
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select your hotel")
                .setItems(displayNames, (dialog, which) -> {
                    // Fill inputs with the selected hotel data
                    TempHotel selected = hotels.get(which);
                    etHName.setText(selected.name);
                    etHCity.setText(selected.city);
                    etHCountry.setText(selected.country);
                    etHStreet.setText(selected.street);
                    etHPostal.setText(selected.postcode);
                    etHPhoneP.setText(selected.phone);

                    Toast.makeText(this, "Data imported!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupSpinner() {
        String[] options = {"Person", "Hotel"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerUserType.setAdapter(adapter);

        spinnerUserType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateVisibility(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void handleRegistration() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || pass.length() < 5) {
            Toast.makeText(this, "Valid email and password (min 5 chars) required!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            etConfirm.setError("Passwords do not match!");
            return;
        }

        UserType selectedType = (spinnerUserType.getSelectedItemPosition() == 0) ? UserType.PERSON : UserType.HOTEL;

        executorService.execute(() -> {
            if (userDao.countUsersByEmail(email) > 0) {
                runOnUiThread(() -> Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show());
                return;
            }

            String hashedPassword = PasswordHasher.hashPassword(pass);
            User newUser = new User(email, hashedPassword, selectedType);

            // Primary key is generated here and used as Foreign Key for sub-profiles
            long userId = userDao.insert(newUser);

            if (userId > 0) {
                if (selectedType == UserType.PERSON) {
                    savePerson((int) userId);
                } else {
                    saveHotel((int) userId);
                }
            }
        });
    }

    private void savePerson(int userId) {
        Person person = new Person(
            userId,
            etPFName.getText().toString().trim(),
            etPLName.getText().toString().trim(),
            etPBirth.getText().toString().trim(),
            etPCountry.getText().toString().trim(),
            etPCity.getText().toString().trim(),
            etPStreet.getText().toString().trim(),
            etPPhone.getText().toString().trim(),
            null
        );
        personDao.insert(person);
        finishWithSuccess();
    }

    private void saveHotel(int userId) {
        Hotel hotel = new Hotel(
                userId,
                etHName.getText().toString().trim(),
                etHChain.getText().toString().trim(),
                etHCountry.getText().toString().trim(),
                etHCity.getText().toString().trim(),
                etHPostal.getText().toString().trim(),
                etHStreet.getText().toString().trim(),
                etHPhoneP.getText().toString().trim(),
                etHPhoneS != null ? etHPhoneS.getText().toString().trim() : "",
                etHDesc.getText().toString().trim()
        );
        hotelDao.insert(hotel);
        finishWithSuccess();
    }

    private void finishWithSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("user_type_pos", spinnerUserType.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int savedPos = savedInstanceState.getInt("user_type_pos", 0);
        spinnerUserType.setSelection(savedPos);

        updateVisibility(savedPos);
    }

    private void updateVisibility(int position) {
        if (position == 0) {
            includePerson.setVisibility(View.VISIBLE);
            includeHotel.setVisibility(View.GONE);
        } else {
            includePerson.setVisibility(View.GONE);
            includeHotel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}