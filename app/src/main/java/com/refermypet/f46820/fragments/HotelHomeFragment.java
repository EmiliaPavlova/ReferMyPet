package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.refermypet.f46820.R;
import com.refermypet.f46820.adapters.BookingAdapter;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.viewmodel.UserViewModel;

public class HotelHomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private BookingAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_home, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Upcoming Reservations
        RecyclerView rvUpcoming = view.findViewById(R.id.rv_bookings);
        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        upcomingAdapter = new BookingAdapter(userViewModel);
        rvUpcoming.setAdapter(upcomingAdapter);

        // Past Reservations Link
        View tvViewPast = view.findViewById(R.id.tv_past_reservations);
        if (tvViewPast != null) {
            tvViewPast.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PastBookingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        setupClickListeners();
        return view;
    }

    private void setupClickListeners() {
        upcomingAdapter.setOnItemClickListener(new BookingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookingWithHotel item) {
                // Handles clicking on the ROW - always open Details
                if (item != null && item.booking != null) {
                    navigateToDetails(item.booking.id);
                }
            }

            @Override
            public void onReviewClick(BookingWithHotel item) {
                // Handles clicking on "Add Review" - open Review screen
                if (item != null && item.booking != null) {
                    ReviewDialogFragment dialog = ReviewDialogFragment.newInstance(item.booking.id);
                    dialog.show(getChildFragmentManager(), "review_dialog");
                }
            }
        });
    }

    /**
     * Helper method to handle navigation to the details screen
     */
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

        int userId = userViewModel.getCurrentUserId();
        if (userId > 0) {
            userViewModel.loadDashboardData(userId);
        }

        // Greetings
        userViewModel.getCurrentHotel().observe(getViewLifecycleOwner(), hotel -> {
            TextView tvWelcome = view.findViewById(R.id.tv_welcome);
            if (tvWelcome != null && hotel != null) {
                String welcomeText = getString(R.string.welcome_name, hotel.name);
                tvWelcome.setText(welcomeText);
            }
        });

        // Reservations
        userViewModel.getBookingsList().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                upcomingAdapter.setBookings(list);
            }
        });
    }
}