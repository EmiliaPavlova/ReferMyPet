package com.refermypet.f46820.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.refermypet.f46820.R;
import com.refermypet.f46820.adapters.BookingAdapter;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.model.Referral;
import com.refermypet.f46820.viewmodel.UserViewModel;

import org.jspecify.annotations.NonNull;

public class PastBookingsFragment extends Fragment {
    private UserViewModel userViewModel;
    private BookingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_bookings, container, false);

        RecyclerView rvPastBookings = view.findViewById(R.id.rv_past_bookings_list);
        rvPastBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        adapter = new BookingAdapter(userViewModel);
        rvPastBookings.setAdapter(adapter);

        // Handle clicks on "Add Review" or "View Review"
        adapter.setOnItemClickListener(new BookingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookingWithHotel item) {
                // When clicking the row, show details
                if (item != null && item.booking != null) {
                    navigateToDetails(item.booking.id);
                }
            }

            @Override
            public void onReviewClick(BookingWithHotel item) {
                // When clicking "Add Review", show the dialog
                showReviewDialog(item);
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
        });


        // Observe and load data
        userViewModel.getPastBookingsList().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                adapter.setBookings(bookings);
            }
        });

        userViewModel.loadPastBookingsForHotel(userViewModel.getCurrentUserId());

        return view;
    }

    /**
     * Opens a modal dialog for adding a new review or viewing an existing one.
     * @param item The booking data containing associated person and potential referral.
     */
    private void showReviewDialog(BookingWithHotel item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_review_title);
        RatingBar ratingBar = dialogView.findViewById(R.id.rb_review_rating);
        EditText etComment = dialogView.findViewById(R.id.et_review_comment);
        Button btnSave = dialogView.findViewById(R.id.btn_save_review);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Configure the dialog mode based on whether a review already exists
        if (item.hasReview && item.referrals != null && !item.referrals.isEmpty()) {
            Referral currentRef = item.referrals.get(0);

            // Read-only mode
            tvTitle.setText(getString(R.string.review_title_view));
            ratingBar.setRating(currentRef.getRatingScore());
            ratingBar.setIsIndicator(true); // Disable interaction

            etComment.setText(currentRef.getRecommendationText());
            etComment.setEnabled(false); // Disable typing

            btnSave.setVisibility(View.GONE); // Hide save button
        } else {
            // Edit mode (Add new review)
            tvTitle.setText(getString(R.string.review_title_add));
            ratingBar.setIsIndicator(false);
            etComment.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);

            btnSave.setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                String comment = etComment.getText().toString().trim();

                if (!comment.isEmpty()) {
                    userViewModel.addReview(item, rating, comment);
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }
}
