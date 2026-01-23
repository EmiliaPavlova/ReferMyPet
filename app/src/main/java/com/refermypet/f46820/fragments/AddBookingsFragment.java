package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.refermypet.f46820.R;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.viewmodel.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBookingsFragment extends Fragment {

    private UserViewModel userViewModel;
    private Spinner spinnerHotels;
    private TextView tvSelectedDates;
    private List<Hotel> hotelList = new ArrayList<>();
    private List<Pet> personPets = new ArrayList<>();
    private List<Pet> selectedPets = new ArrayList<>();
    private TextView tvSelectedPets;
    private int savedHotelIndex = -1;
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

        if (savedInstanceState != null) {
            startDate = savedInstanceState.getString("start_date", "");
            endDate = savedInstanceState.getString("end_date", "");
            ArrayList<Pet> tempSelected = (ArrayList<Pet>) savedInstanceState.getSerializable("selected_pets");
            if (tempSelected != null) {
                selectedPets = tempSelected;
                updateSelectedPetsText();
            }
            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                tvSelectedDates.setText(getString(R.string.selected_dates_format, startDate, endDate));
            }
            savedHotelIndex = savedInstanceState.getInt("selected_hotel_index", -1);
        }

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("start_date", startDate);
        outState.putString("end_date", endDate);
        outState.putSerializable("selected_pets", new ArrayList<>(selectedPets));
        outState.putInt("selected_hotel_index", spinnerHotels.getSelectedItemPosition());
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
            checkedItems[i] = isPetSelected(personPets.get(i));
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_pets)
                .setMultiChoiceItems(petNames, checkedItems, (dialog, which, isChecked) -> {
                    Pet currentPet = personPets.get(which);
                    if (isChecked) {
                        if (!isPetSelected(currentPet)) {
                            selectedPets.add(currentPet);
                        }
                    } else {
                        removePetFromSelected(currentPet);
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> updateSelectedPetsText())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean isPetSelected(Pet pet) {
        for (Pet p : selectedPets) {
            if (p.id == pet.id) return true;
        }
        return false;
    }

    private void removePetFromSelected(Pet pet) {
        for (int i = 0; i < selectedPets.size(); i++) {
            if (selectedPets.get(i).id == pet.id) {
                selectedPets.remove(i);
                break;
            }
        }
    }

    private void updateSelectedPetsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedPets.size(); i++) {
            sb.append(selectedPets.get(i).name);
            if (i < selectedPets.size() - 1) sb.append(", ");
        }
        tvSelectedPets.setText(sb.toString());
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

                if (savedHotelIndex != -1) {
                    spinnerHotels.setSelection(savedHotelIndex);
                }
            }
        });
    }

    private void showDatePicker() {
        // Only from current day on
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(getString(R.string.title_select_period))
                .setCalendarConstraints(constraintsBuilder.build())
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

        userViewModel.addBooking(personId, selectedHotel.getId(), startDate, endDate, new ArrayList<>(selectedPets));

        String successMsg = getString(R.string.booking_success, selectedHotel.getName());
        Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();

        getParentFragmentManager().popBackStack();
    }
}