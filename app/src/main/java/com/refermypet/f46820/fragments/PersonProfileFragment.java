package com.refermypet.f46820.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.refermypet.f46820.R;
import com.refermypet.f46820.enums.PetType;
import com.refermypet.f46820.viewmodel.UserViewModel;
import com.refermypet.f46820.model.Person;
import com.refermypet.f46820.model.Pet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PersonProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private EditText etFirstName, etLastName, etBirthDate, etCountry, etCity, etStreet, etPhone;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private LinearLayout passwordChangeLayout, petsContainer;
    private TextView tvChangePasswordLink;
    private Button btnAddPet, btnSaveProfile;

    private int petCount = 0;
    private static final int MAX_PETS = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_person, container, false);

        initializeViews(view);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupListeners(inflater);
        observeUserData(inflater, savedInstanceState);

        if (savedInstanceState != null) {
            passwordChangeLayout.setVisibility(savedInstanceState.getInt("pass_vis", View.GONE));
            ArrayList<Pet> savedPets = (ArrayList<Pet>) savedInstanceState.getSerializable("temp_pets");
            if (savedPets != null) {
                loadUserPets(savedPets, inflater);
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pass_vis", passwordChangeLayout.getVisibility());
        ArrayList<Pet> currentPets = new ArrayList<>();
        for (int i = 0; i < petsContainer.getChildCount(); i++) {
            currentPets.add(getPetFromView(petsContainer.getChildAt(i)));
        }
        outState.putSerializable("temp_pets", currentPets);
    }

    private void initializeViews(View view) {
        etFirstName = view.findViewById(R.id.et_person_first_name);
        etLastName = view.findViewById(R.id.et_person_last_name);
        etBirthDate = view.findViewById(R.id.et_person_birth_date);
        etCountry = view.findViewById(R.id.et_person_country);
        etCity = view.findViewById(R.id.et_person_city);
        etStreet = view.findViewById(R.id.et_person_street);
        etPhone = view.findViewById(R.id.et_person_phone);

        tvChangePasswordLink = view.findViewById(R.id.tv_change_password_link);
        passwordChangeLayout = view.findViewById(R.id.layout_password_change);
        etOldPassword = view.findViewById(R.id.et_old_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);

        petsContainer = view.findViewById(R.id.pets_container);
        btnAddPet = view.findViewById(R.id.btn_add_pet);
        btnSaveProfile = view.findViewById(R.id.btn_save_person_profile);
    }

    private void observeUserData(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {
        int userId = userViewModel.getCurrentUserId();
        userViewModel.loadUserProfile(userId);

        userViewModel.getCurrentPerson().observe(getViewLifecycleOwner(), person -> {
            if (person != null && savedInstanceState == null) {
                etFirstName.setText(person.firstName);
                etLastName.setText(person.lastName);
                etBirthDate.setText(person.birthDate);
                etCountry.setText(person.country);
                etCity.setText(person.city);
                etStreet.setText(person.streetAddress);
                etPhone.setText(person.phone);
            }
        });

        userViewModel.getUserPets().observe(getViewLifecycleOwner(), pets -> {
            if (pets != null && savedInstanceState == null) {
                loadUserPets(pets, inflater);
            }
        });
    }

    private void loadUserPets(List<Pet> pets, LayoutInflater inflater) {
        petsContainer.removeAllViews();
        petCount = 0;

        if (pets != null && !pets.isEmpty()) {
            for (Pet pet : pets) {
                addPetView(inflater, pet);
            }
        }
    }

    private void setupListeners(LayoutInflater inflater) {
        tvChangePasswordLink.setOnClickListener(v -> {
            int visibility = (passwordChangeLayout.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
            passwordChangeLayout.setVisibility(visibility);
        });

        btnAddPet.setOnClickListener(v -> {
            if (petCount < MAX_PETS) addPetView(inflater, null);
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void addPetView(LayoutInflater inflater, @Nullable Pet pet) {
        View petView = inflater.inflate(R.layout.item_pet_input, petsContainer, false);
        Spinner spinner = petView.findViewById(R.id.spinner_pet_type);
        EditText etName = petView.findViewById(R.id.et_pet_name);
        EditText etBreed = petView.findViewById(R.id.et_pet_breed);
        EditText etBirth = petView.findViewById(R.id.et_pet_birth_date);
        EditText etChip = petView.findViewById(R.id.et_pet_chip);
        ImageButton btnRemove = petView.findViewById(R.id.btn_remove_pet);

        etBirth.setFocusable(false);
        etBirth.setClickable(true);
        etBirth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, day) -> {
                String selectedDate = String.format("%02d/%02d/%d", day, month + 1, year);
                etBirth.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        ArrayAdapter<PetType> adapter = new ArrayAdapter<>(
                spinner.getContext(),
                android.R.layout.simple_spinner_item,
                PetType.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (pet != null) {
            etName.setText(pet.name);
            etBreed.setText(pet.breed);
            etBirth.setText(pet.birthDate);
            etChip.setText(pet.chipNumber);
            spinner.setSelection(pet.type.ordinal());
            petView.setTag(pet.id);
        }

        btnRemove.setOnClickListener(v -> {
            petsContainer.removeView(petView);
            petCount--;
        });

        petsContainer.addView(petView);
        petCount++;
    }

    private Pet getPetFromView(View petView) {
        String name = ((EditText) petView.findViewById(R.id.et_pet_name)).getText().toString().trim();
        String breed = ((EditText) petView.findViewById(R.id.et_pet_breed)).getText().toString().trim();
        String birth = ((EditText) petView.findViewById(R.id.et_pet_birth_date)).getText().toString().trim();
        String chip = ((EditText) petView.findViewById(R.id.et_pet_chip)).getText().toString().trim();
        PetType type = (PetType) ((Spinner) petView.findViewById(R.id.spinner_pet_type)).getSelectedItem();
        Pet pet = new Pet(0, name, breed, type, birth, chip, "", 0.0f);
        if (petView.getTag() != null) pet.id = (int) petView.getTag();
        return pet;
    }

    /**
     * Logic for gathering data from all fields and updating the local database.
     * After successful update, it navigates back to the previous screen.
     */
    private void saveProfile() {
        Person person = userViewModel.getCurrentPerson().getValue();
        if (person == null) return;

        // Password change logic
        if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.updateUserPassword(userViewModel.getCurrentUserId(), oldPass, newPass);
        }

        // Update profile data
        person.firstName = etFirstName.getText().toString().trim();
        person.lastName = etLastName.getText().toString().trim();
        person.birthDate = etBirthDate.getText().toString().trim();
        person.country = etCountry.getText().toString().trim();
        person.city = etCity.getText().toString().trim();
        person.streetAddress = etStreet.getText().toString().trim();
        person.phone = etPhone.getText().toString().trim();

        // Update pets
        List<Pet> updatedPets = new ArrayList<>();
        for (int i = 0; i < petsContainer.getChildCount(); i++) {
            Pet pet = getPetFromView(petsContainer.getChildAt(i));
            if (!pet.name.isEmpty()) {
                pet.ownerId = person.id;
                updatedPets.add(pet);
            }
        }

        // Persist changes via ViewModel
        userViewModel.updatePerson(person);
        userViewModel.updatePets(person.id, updatedPets);

        // Notify user and navigate back
        Toast.makeText(getContext(), getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();

        if (isAdded()) {
            getParentFragmentManager().popBackStack();
        }
    }
}