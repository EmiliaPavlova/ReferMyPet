package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.refermypet.f46820.R;
import com.refermypet.f46820.model.Hotel;
import com.refermypet.f46820.viewmodel.UserViewModel;

public class HotelProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private ImageView ivHotelPhoto;
    private TextInputEditText etName, etChain, etCountry, etCity, etPostal, etStreet, etPhoneP, etPhoneS, etDesc;
    private TextInputEditText etOldPass, etNewPass, etConfirmPass;
    private Button btnSearchHotel, btnSaveProfile;
    private ProgressBar pbLoading;
    private LinearLayout passwordChangeLayout;
    private TextView tvChangePasswordLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_hotel, container, false);

        initializeViews(view);

        // Initialize ViewModel from activity context
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupListeners();
        observeHotelData(savedInstanceState);

        if (savedInstanceState != null) {
            passwordChangeLayout.setVisibility(savedInstanceState.getInt("h_pass_vis", View.GONE));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (passwordChangeLayout != null) {
            outState.putInt("h_pass_vis", passwordChangeLayout.getVisibility());
        }
    }

    /**
     * Link all UI components from the layout XML to Java objects.
     */
    private void initializeViews(View view) {
        ivHotelPhoto = view.findViewById(R.id.iv_h_photo);
        etName = view.findViewById(R.id.et_h_name);

        btnSearchHotel = view.findViewById(R.id.btn_search_hotel);
        if (btnSearchHotel != null) {
            btnSearchHotel.setVisibility(View.GONE);
        }

        pbLoading = view.findViewById(R.id.pb_loading);
        if (pbLoading != null) {
            pbLoading.setVisibility(View.GONE);
        }

        // Main hotel information fields
        etChain = view.findViewById(R.id.et_h_chain);
        etCountry = view.findViewById(R.id.et_h_country);
        etCity = view.findViewById(R.id.et_h_city);
        etPostal = view.findViewById(R.id.et_h_postal);
        etStreet = view.findViewById(R.id.et_h_street);
        etPhoneP = view.findViewById(R.id.et_h_phone_p);
        etPhoneS = view.findViewById(R.id.et_h_phone_s);
        etDesc = view.findViewById(R.id.et_h_desc);

        // Password change section
        tvChangePasswordLink = view.findViewById(R.id.tv_h_change_password_link);
        passwordChangeLayout = view.findViewById(R.id.layout_h_password_change);
        etOldPass = view.findViewById(R.id.et_h_old_pass);
        etNewPass = view.findViewById(R.id.et_h_new_pass);
        etConfirmPass = view.findViewById(R.id.et_h_confirm_pass);

        btnSaveProfile = view.findViewById(R.id.btn_save_h_profile);
    }

    /**
     * Observe the Hotel object from ViewModel and populate the UI fields.
     */
    private void observeHotelData(@Nullable Bundle savedInstanceState) {
        int userId = userViewModel.getCurrentUserId();
        userViewModel.loadUserProfile(userId);

        userViewModel.getCurrentHotel().observe(getViewLifecycleOwner(), hotel -> {
            if (hotel != null && savedInstanceState == null) {
                etName.setText(hotel.name);
                etChain.setText(hotel.chain);
                etCountry.setText(hotel.country);
                etCity.setText(hotel.city);
                etPostal.setText(hotel.postalCode);
                etStreet.setText(hotel.streetAddress);
                etPhoneP.setText(hotel.phonePrimary);
                etPhoneS.setText(hotel.phoneSecondary);
                etDesc.setText(hotel.description);
            }
        });
    }

    /**
     * Define click listeners and interactive logic.
     */
    private void setupListeners() {
        // Toggle visibility of the password change section
        if (tvChangePasswordLink != null && passwordChangeLayout != null) {
            tvChangePasswordLink.setOnClickListener(v -> {
                if (passwordChangeLayout.getVisibility() == View.GONE) {
                    passwordChangeLayout.setVisibility(View.VISIBLE);
                } else {
                    passwordChangeLayout.setVisibility(View.GONE);
                }
            });
        }

        // Search hotel logic
        if (btnSearchHotel != null) {
            btnSearchHotel.setOnClickListener(v -> {
                Toast.makeText(getContext(), getString(R.string.searching_hotel_data), Toast.LENGTH_SHORT).show();
            });
        }

        // Save profile logic
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveHotelProfile());
        }
    }

    /**
     * Validates and saves the updated hotel profile information.
     */
    private void saveHotelProfile() {
        Hotel hotel = userViewModel.getCurrentHotel().getValue();
        if (hotel == null) return;

        // Password change logic
        if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
            String oldPass = etOldPass.getText().toString().trim();
            String newPass = etNewPass.getText() != null ? etNewPass.getText().toString().trim() : "";
            String confirmPass = etConfirmPass.getText() != null ? etConfirmPass.getText().toString() : "";

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all password fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.updateUserPassword(userViewModel.getCurrentUserId(), oldPass, newPass);
        }

        // Update hotel profile
        hotel.name = etName.getText().toString().trim();
        hotel.chain = etChain.getText().toString().trim();
        hotel.country = etCountry.getText().toString().trim();
        hotel.city = etCity.getText().toString().trim();
        hotel.postalCode = etPostal.getText().toString().trim();
        hotel.streetAddress = etStreet.getText().toString().trim();
        hotel.phonePrimary = etPhoneP.getText().toString().trim();
        hotel.phoneSecondary = etPhoneS.getText().toString().trim();
        hotel.description = etDesc.getText().toString().trim();

        // Save to database via ViewModel
        userViewModel.updateHotel(hotel);

        Toast.makeText(getContext(), getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();

        if (isAdded()) {
            getParentFragmentManager().popBackStack();
        }
    }
}