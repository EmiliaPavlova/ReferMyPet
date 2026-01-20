package com.refermypet.f46820.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.refermypet.f46820.R;
import com.refermypet.f46820.model.Pet;
import java.util.List;
import java.util.Locale;

public class BookingPetsAdapter extends RecyclerView.Adapter<BookingPetsAdapter.PetViewHolder> {

    private final List<Pet> pets;

    public BookingPetsAdapter(List<Pet> pets) {
        this.pets = pets;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        Context context = holder.itemView.getContext();

        holder.tvNameType.setText(String.format("%s (%s)", pet.name, pet.type));

        holder.tvBreed.setText(context.getString(R.string.pet_breed_label, pet.breed));

        holder.tvAge.setText(context.getString(R.string.pet_birth_date_label, pet.birthDate));

        holder.tvChip.setText(context.getString(R.string.pet_chip_label, pet.chipNumber));

        holder.tvRating.setText(String.format(Locale.US, "%.1f", pet.rating));

        holder.ivIcon.setImageResource(R.drawable.ic_default_pet_placeholder);
    }

    @Override
    public int getItemCount() {
        return pets != null ? pets.size() : 0;
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvNameType, tvBreed, tvAge, tvChip, tvRating;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_pet_icon);
            tvNameType = itemView.findViewById(R.id.tv_pet_name_type);
            tvBreed = itemView.findViewById(R.id.tv_pet_breed);
            tvAge = itemView.findViewById(R.id.tv_pet_age);
            tvChip = itemView.findViewById(R.id.tv_pet_chip);
            tvRating = itemView.findViewById(R.id.tv_pet_rating);
        }
    }
}