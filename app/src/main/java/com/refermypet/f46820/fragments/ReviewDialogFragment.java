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
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.refermypet.f46820.R;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.viewmodel.UserViewModel;

import java.util.List;

public class ReviewDialogFragment extends DialogFragment {

    private static final String ARG_BOOKING_ID = "booking_id";
    private int bookingId;
    private BookingWithHotel bookingItem;
    private UserViewModel userViewModel;

    public static ReviewDialogFragment newInstance(int bookingId) {
        ReviewDialogFragment fragment = new ReviewDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOKING_ID, bookingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getInt(ARG_BOOKING_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Find the specific booking item from ViewModel's current list
        List<BookingWithHotel> list = userViewModel.getBookingsList().getValue();
        if (list != null) {
            for (BookingWithHotel b : list) {
                if (b.booking != null && b.booking.id == bookingId) {
                    bookingItem = b;
                    break;
                }
            }
        }

        if (bookingItem == null) {
            dismiss();
            return;
        }

        TextView tvTitle = view.findViewById(R.id.tv_review_title);
        RatingBar ratingBar = view.findViewById(R.id.rb_review_rating);
        EditText etComment = view.findViewById(R.id.et_review_comment);
        Button btnSave = view.findViewById(R.id.btn_save_review);
        ImageButton btnClose = view.findViewById(R.id.btn_close_dialog);

        // Format pets names for the title
        StringBuilder petsName = new StringBuilder();
        List<Pet> pets = bookingItem.getPets();
        if (pets != null && !pets.isEmpty()) {
            for (int i = 0; i < pets.size(); i++) {
                petsName.append(pets.get(i).name);
                if (i < pets.size() - 1) petsName.append(", ");
            }
        }

        String personName = (bookingItem.person != null) ? bookingItem.person.getFirstName() : "Guest";
        tvTitle.setText(getString(R.string.rate_guest_and_pets, personName, petsName.toString()));

        btnSave.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            // Save to DB and refresh dashboard to trigger Observers
            userViewModel.addReview(bookingItem, rating, comment);
            userViewModel.loadDashboardData(userViewModel.getCurrentUserId());

            dismiss();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}