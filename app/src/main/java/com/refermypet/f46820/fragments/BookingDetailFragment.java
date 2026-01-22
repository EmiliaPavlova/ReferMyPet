package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.refermypet.f46820.R;
import com.refermypet.f46820.adapters.BookingPetsAdapter;
import com.refermypet.f46820.model.Referral;
import com.refermypet.f46820.viewmodel.UserViewModel;

public class BookingDetailFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextView tvHotelName, tvLocation, tvDate, tvGuestName, tvReview;
    private RecyclerView rvPets;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_detail, container, false);

        tvHotelName = view.findViewById(R.id.tv_detail_hotel_name);
        tvLocation = view.findViewById(R.id.tv_detail_location);
        tvDate = view.findViewById(R.id.tv_detail_date);
        tvGuestName = view.findViewById(R.id.tv_detail_guest_name);
        tvReview = view.findViewById(R.id.tv_detail_review);
        rvPets = view.findViewById(R.id.rv_booking_pets);
        rvPets.setLayoutManager(new LinearLayoutManager(getContext()));

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        rvPets = view.findViewById(R.id.rv_booking_pets);
        rvPets.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            int bookingId = getArguments().getInt("BOOKING_ID", -1);
            if (bookingId != -1) {
                userViewModel.loadBookingById(bookingId);
                userViewModel.getSelectedBooking().observe(getViewLifecycleOwner(), item -> {
                    if (item != null) {
                        if (item.hotel != null) {
                            tvHotelName.setText(item.hotel.getName());
                            tvLocation.setText(getString(R.string.detail_location, item.hotel.getCity(), item.hotel.getCountry()));
                        }

                        if (item.booking != null) {
                            tvDate.setText(getString(R.string.detail_period, item.booking.startDate, item.booking.endDate));
                        }

                        if (item.person != null) {
                            tvGuestName.setText(getString(R.string.detail_guest, item.person.getFirstName(), item.person.getLastName()));
                        }

                        if (item.booking != null && item.booking.selectedPets != null && !item.booking.selectedPets.isEmpty()) {
                            rvPets.setVisibility(View.VISIBLE);
                            BookingPetsAdapter adapter = new BookingPetsAdapter(item.booking.selectedPets, item.referrals);
                            rvPets.setAdapter(adapter);
                        } else {
                            rvPets.setVisibility(View.GONE);
                        }

                        RatingBar rbDetailReview = view.findViewById(R.id.rb_detail_review_stars);

                        if (item.referrals != null && !item.referrals.isEmpty()) {
                            Referral firstRef = item.referrals.get(0); // Вземаме първия запис

                            tvReview.setText(getString(R.string.detail_review_label, firstRef.recommendationText));

                            if (rbDetailReview != null) {
                                rbDetailReview.setVisibility(View.VISIBLE);
                                rbDetailReview.setRating(firstRef.ratingScore); // Вече е ratingScore
                            }
                        } else {
                            tvReview.setText(getString(R.string.no_review_given));
                            if (rbDetailReview != null) rbDetailReview.setVisibility(View.GONE);
                        }

                        if (item.booking != null && item.booking.selectedPets != null) {
                            BookingPetsAdapter adapter = new BookingPetsAdapter(item.booking.selectedPets, item.referrals);
                            rvPets.setAdapter(adapter);
                        }
                    }
                });
            }
        }
    }
}