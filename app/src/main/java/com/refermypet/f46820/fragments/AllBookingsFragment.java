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

/**
 * Fragment responsible for displaying the full history of bookings (past and upcoming).
 */
public class AllBookingsFragment extends Fragment {

    private UserViewModel userViewModel;
    private RecyclerView rvAllBookings;
    private BookingAdapter bookingAdapter;
    private TextView tvNoData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_bookings, container, false);

        // Initialize UI components
        rvAllBookings = view.findViewById(R.id.rv_all_bookings);
        tvNoData = view.findViewById(R.id.tv_no_all_bookings);

        // Set up RecyclerView with a vertical layout manager
        rvAllBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        // Use the activity-scoped ViewModel to access shared user data
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Initialize the adapter with the ViewModel to support delete actions
        bookingAdapter = new BookingAdapter(userViewModel);
        rvAllBookings.setAdapter(bookingAdapter);

        setupObservers();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch the current person ID from the ViewModel and trigger data loading
        int userId = userViewModel.getCurrentUserId();
        if (userId != 0) {
            userViewModel.loadAllBookings(userId);
        }
    }

    /**
     * Observes the LiveData from ViewModel to update the list of all bookings.
     */
    private void setupObservers() {
        userViewModel.getAllBookingsList().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null && !bookings.isEmpty()) {
                rvAllBookings.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                bookingAdapter.setBookings(bookings);
            } else {
                rvAllBookings.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
        });

        // Handle clicks on individual booking items to navigate to details
        bookingAdapter.setOnItemClickListener(new BookingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookingWithHotel item) {
                // Navigate to details when the whole row is clicked
                if (item != null && item.booking != null) {
                    BookingDetailFragment detailFragment = new BookingDetailFragment();

                    // Pass the selected booking ID to the detail fragment
                    Bundle args = new Bundle();
                    args.putInt("BOOKING_ID", item.booking.id);
                    detailFragment.setArguments(args);

                    getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
                }
            }

            @Override
            public void onReviewClick(BookingWithHotel item) {
            }
        });
    }
}