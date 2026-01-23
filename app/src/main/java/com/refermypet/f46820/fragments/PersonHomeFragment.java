package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.refermypet.f46820.R;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.viewmodel.UserViewModel;

/**
 * HomeFragment serves as the main dashboard for the user.
 * Implements MVVM pattern with a RecyclerView for bookings.
 */
public class PersonHomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextView tvWelcome;

    // Reservations (List instead of single view)
    private TextView tvNoReservations;
    private View layoutUpcomingBooking;

    // Referrals (Single view - latest)
    private TextView tvNoReferrals;
    private LinearLayout layoutLatestReferral;
    private RatingBar ratingBar;
    private TextView tvReferralText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person_home, container, false);

        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvNoReservations = view.findViewById(R.id.tv_no_reservations);
        layoutUpcomingBooking = view.findViewById(R.id.layout_upcoming_booking);

        tvNoReferrals = view.findViewById(R.id.tv_no_referrals);
        layoutLatestReferral = view.findViewById(R.id.layout_latest_referral);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvReferralText = view.findViewById(R.id.tv_referral_text);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        setupObservers();
        setupClickListeners();

        return view;
    }

    private void setupObservers() {
        // Greetings
        userViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                tvWelcome.setText(getString(R.string.welcome_name, name));
            }
        });

        // Bookings
        userViewModel.getBookingsList().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null && !bookings.isEmpty()) {
                layoutUpcomingBooking.setVisibility(View.VISIBLE);
                tvNoReservations.setVisibility(View.GONE);

                BookingWithHotel latest = bookings.get(0);

                TextView tvHotelName = layoutUpcomingBooking.findViewById(R.id.tv_name);
                TextView tvBookingDates = layoutUpcomingBooking.findViewById(R.id.tv_booking_dates);
                TextView tvPets = layoutUpcomingBooking.findViewById(R.id.tv_selected_pets);
                View ivDelete = layoutUpcomingBooking.findViewById(R.id.iv_delete_booking);

                if (latest.hotel != null) {
                    tvHotelName.setText(latest.hotel.getName());
                    TextView tvCity = layoutUpcomingBooking.findViewById(R.id.tv_hotel_city);
                    if (tvCity != null) tvCity.setText(latest.hotel.getCity());
                }
                String dateRange = latest.booking.startDate + " - " + latest.booking.endDate;
                tvBookingDates.setText(dateRange);

                if (tvPets != null) {
                    if (latest.booking.selectedPets != null && !latest.booking.selectedPets.isEmpty()) {
                        StringBuilder sb = new StringBuilder(getString(R.string.pets)).append(" ");
                        for (int i = 0; i < latest.booking.selectedPets.size(); i++) {
                            sb.append(latest.booking.selectedPets.get(i).name);
                            if (i < latest.booking.selectedPets.size() - 1) sb.append(", ");
                        }
                        tvPets.setText(sb.toString());
                    } else {
                        tvPets.setText(getString(R.string.pets_none));
                    }
                }

                View btnDelete = layoutUpcomingBooking.findViewById(R.id.iv_delete_booking);
                if (btnDelete != null) {
                    btnDelete.setOnClickListener(v -> {
                        userViewModel.deleteBooking(latest.booking);
                        userViewModel.loadAllBookings(userViewModel.getCurrentUserId());

//                        Toast.makeText(getContext(), R.string.booking_deleted, Toast.LENGTH_SHORT).show();
                    });
                }

                layoutUpcomingBooking.setOnClickListener(v -> navigateToDetails(latest.booking.id));

            } else {
                layoutUpcomingBooking.setVisibility(View.GONE);
                tvNoReservations.setVisibility(View.VISIBLE);
            }
        });

        // Referral
        userViewModel.getAllBookingsList().observe(getViewLifecycleOwner(), allBookings -> {
            if (allBookings != null) {
                float avgRating = userViewModel.getCalculatedAverageRating();
                String latestText = userViewModel.getLatestReferralText();

                if (latestText == null) {
                    tvNoReferrals.setVisibility(View.VISIBLE);
                    layoutLatestReferral.setVisibility(View.GONE);
                    ratingBar.setRating(0);
                    tvReferralText.setText("");
                } else {
                    tvNoReferrals.setVisibility(View.GONE);
                    layoutLatestReferral.setVisibility(View.VISIBLE);

                    ratingBar.setRating(avgRating);
                    tvReferralText.setText(latestText);

                    View tvViewAll = getView() != null ? getView().findViewById(R.id.tv_view_all_referrals) : null;
                    if (tvViewAll != null) {
                        tvViewAll.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        // Not implemented for person home
    }

    private void navigateToDetails(int bookingId) {
        Bundle args = new Bundle();
        args.putInt("BOOKING_ID", bookingId);

        BookingDetailFragment detailFragment = new BookingDetailFragment();
        detailFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add New Reservation Link
        View addReservationLink = view.findViewById(R.id.tv_add_reservation);
        if (addReservationLink != null) {
            addReservationLink.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddBookingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // View All Reservations Link
        View viewAllReservations = view.findViewById(R.id.tv_view_all_bookings);
        if (viewAllReservations != null) {
            viewAllReservations.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AllBookingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        userViewModel.loadAllBookings(userViewModel.getCurrentUserId());
    }
}