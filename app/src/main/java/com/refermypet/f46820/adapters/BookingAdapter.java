package com.refermypet.f46820.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.refermypet.f46820.R;
import com.refermypet.f46820.enums.UserType;
import com.refermypet.f46820.model.BookingWithHotel;
import com.refermypet.f46820.model.Pet;
import com.refermypet.f46820.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying booking records in a RecyclerView.
 * Supports click listeners for navigation and review actions.
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<BookingWithHotel> bookings = new ArrayList<>();
    private final UserViewModel viewModel;
    private OnItemClickListener listener;

    public BookingAdapter(UserViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClickListener {
        void onItemClick(BookingWithHotel booking); // For details
        void onReviewClick(BookingWithHotel booking); // For reviews
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingWithHotel item = bookings.get(position);
        android.content.Context context = holder.itemView.getContext();

        // Format and display reservation dates
        if (item.booking != null) {
            String period = context.getString(R.string.reservation_format,
                    item.booking.startDate,
                    item.booking.endDate);
            holder.tvBookingDates.setText(period);
        }

        // Identify the current user role to determine UI logic
        UserType role = viewModel.getUserType().getValue();

        // Configure UI based on the user's role (HOTEL vs PERSON)
        if (role == UserType.HOTEL) {
            // Hotel View: Display Guest information
            if (item.person != null) {
                String guestName = item.person.getFirstName() + " " + item.person.getLastName();
                holder.tvHotelName.setText(guestName); // Refers to R.id.tv_name
                holder.tvHotelCity.setText(item.person.getCity()); // Refers to R.id.tv_hotel_city
            }

            // Logic to hide the "Add Review" button if a referral already exists
            boolean hasReview = item.referrals != null && !item.referrals.isEmpty();
            if (hasReview) {
                holder.tvAddReview.setVisibility(View.GONE);
            } else {
                holder.tvAddReview.setVisibility(View.VISIBLE);
                holder.tvAddReview.setText(context.getString(R.string.add_review));
            }
        } else {
            // Person View: Display Hotel information
            if (item.hotel != null) {
                String hotelName = context.getString(R.string.hotel_name_display, item.hotel.getName());
                holder.tvHotelName.setText(hotelName);
                holder.tvHotelCity.setText(item.hotel.getCity());
            }

            // Hide the review link for Person users
            holder.tvAddReview.setVisibility(View.GONE);
        }

        // List selected pets
        List<Pet> pets = item.booking.selectedPets;
        String label = context.getString(R.string.pets);
        if (pets != null && !pets.isEmpty()) {
            StringBuilder sb = new StringBuilder(label);
            if (!label.endsWith(" ")) sb.append(" ");

            for (int i = 0; i < pets.size(); i++) {
                sb.append(pets.get(i).name);
                if (i < pets.size() - 1) sb.append(", ");
            }
            holder.tvPets.setText(sb.toString());
        } else {
            String none = context.getString(R.string.none);
            String finalText = label.endsWith(" ") ? label + none : label + " " + none;
            holder.tvPets.setText(finalText);
        }

        // Setup Click Listeners
        holder.ivDelete.setOnClickListener(v -> viewModel.deleteBooking(item.booking));

        // Click on the Review link -> Open Review Screen
        holder.tvAddReview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(item);
            }
        });

        // Click on the whole row -> Open Details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void setBookings(List<BookingWithHotel> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvHotelName, tvHotelCity, tvBookingDates, tvPets, tvAddReview;
        ImageView ivDelete;

        BookingViewHolder(View itemView) {
            super(itemView);
            // Binding IDs from item_booking.xml
            tvHotelName = itemView.findViewById(R.id.tv_name);
            tvHotelCity = itemView.findViewById(R.id.tv_hotel_city);
            tvBookingDates = itemView.findViewById(R.id.tv_booking_dates);
            tvPets = itemView.findViewById(R.id.tv_selected_pets);
            ivDelete = itemView.findViewById(R.id.iv_delete_booking);
            tvAddReview = itemView.findViewById(R.id.tv_add_review);
        }
    }
}