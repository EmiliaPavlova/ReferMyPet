package com.refermypet.f46820.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.refermypet.f46820.BookingReminderService;
import com.refermypet.f46820.R;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.viewmodel.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBookingFragment extends Fragment {

    private UserViewModel userViewModel;
    private Spinner spinnerHotels;
    private TextView tvSelectedDates;
    private List<Hotel> hotelList = new ArrayList<>();
    private List<Pet> personPets = new ArrayList<>();
    private List<Pet> selectedPets = new ArrayList<>();
    private TextView tvSelectedPets;

    private String startDate = "";
    private String endDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        spinnerHotels = view.findViewById(R.id.spinner_hotels);
        tvSelectedDates = view.findViewById(R.id.tv_selected_dates);
        tvSelectedPets = view.findViewById(R.id.tv_selected_pets);

        Button btnSelectDates = view.findViewById(R.id.btn_select_dates);
        Button btnSelectPets = view.findViewById(R.id.btn_select_pets);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_booking);

        setupHotelSpinner();

        // Load user pets to ensure the selection dialog has data
        int userId = userViewModel.getCurrentUserId();
        userViewModel.loadUserProfile(userId);

        userViewModel.getUserPets().observe(getViewLifecycleOwner(), pets -> {
            if (pets != null) {
                this.personPets = pets;
            }
        });

        btnSelectDates.setOnClickListener(v -> showDatePicker());

        btnSelectPets.setOnClickListener(v -> showPetSelectionDialog());

        btnConfirm.setOnClickListener(v -> {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.error_select_dates), Toast.LENGTH_SHORT).show();
                return;
            }
            saveBooking();
        });
    }

    private void showPetSelectionDialog() {
        if (personPets.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_pets_found, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] petNames = new String[personPets.size()];
        boolean[] checkedItems = new boolean[personPets.size()];

        for (int i = 0; i < personPets.size(); i++) {
            petNames[i] = personPets.get(i).name;
            checkedItems[i] = selectedPets.contains(personPets.get(i));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_pets)
                .setMultiChoiceItems(petNames, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedPets.contains(personPets.get(which))) {
                            selectedPets.add(personPets.get(which));
                        }
                    } else {
                        selectedPets.remove(personPets.get(which));
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < selectedPets.size(); i++) {
                        sb.append(selectedPets.get(i).name);
                        if (i < selectedPets.size() - 1) sb.append(", ");
                    }
                    tvSelectedPets.setText(sb.toString());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupHotelSpinner() {
        userViewModel.loadAllHotels();
        userViewModel.getAllHotelsList().observe(getViewLifecycleOwner(), hotels -> {
            if (hotels != null && !hotels.isEmpty()) {
                this.hotelList = hotels;
                List<String> hotelNames = new ArrayList<>();
                for (Hotel h : hotels) {
                    hotelNames.add(h.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        hotelNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerHotels.setAdapter(adapter);
            }
        });
    }

    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.title_select_period))
                .build();

        picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startDate = sdf.format(new Date(selection.first));
            endDate = sdf.format(new Date(selection.second));

            String formattedRange = getString(R.string.selected_dates_format, startDate, endDate);
            tvSelectedDates.setText(formattedRange);
        });
    }

    private void saveBooking() {
        int selectedIndex = spinnerHotels.getSelectedItemPosition();
        if (selectedIndex < 0) return;

        Hotel selectedHotel = hotelList.get(selectedIndex);
        int personId = userViewModel.getCurrentPersonId();

        userViewModel.addBooking(personId, selectedHotel.getId(), startDate, endDate, selectedPets);

        String successMsg = getString(R.string.booking_success, selectedHotel.getName());
        Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();

        getParentFragmentManager().popBackStack();
    }
}