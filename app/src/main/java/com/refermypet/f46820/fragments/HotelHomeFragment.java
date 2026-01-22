package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
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
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.viewmodel.UserViewModel;

import java.util.List;

public class HotelHomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private BookingAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_home, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Upcoming
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
                // This handles clicking on the ROW - Always open Details
                if (item != null && item.booking != null) {
                    navigateToDetails(item.booking.id);
                }
            }

            @Override
            public void onReviewClick(BookingWithHotel item) {
                // This handles clicking on "Add Review" - Open Review screen
                if (item != null && item.booking != null) {
                    showReviewDialog(item);
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

    /**
     * Shows the review dialog where the hotel can rate the guest and their pets.
     */
    private void showReviewDialog(BookingWithHotel item) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tv_review_title);
        RatingBar ratingBar = dialogView.findViewById(R.id.rb_review_rating);
        EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        Button btnSave = dialogView.findViewById(R.id.btn_save_review);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        // pets names
        StringBuilder petsName = new StringBuilder();
        List<Pet> pets = item.getPets();
        if (pets != null && !pets.isEmpty()) {
            for (int i = 0; i < pets.size(); i++) {
                petsName.append(pets.get(i).name);
                if (i < pets.size() - 1) petsName.append(", ");
            }
        }

        String personName = (item.person != null) ? item.person.getFirstName() : "Guest";

        String formattedTitle = getString(R.string.rate_guest_and_pets, personName, petsName.toString());
        tvTitle.setText(formattedTitle);

        btnSave.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();
            userViewModel.addReview(item, rating, comment);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}