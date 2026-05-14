package com.example.campuscore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuscore.databinding.ItemFeatureCardBinding;
import com.example.campuscore.models.FeatureCard;

import java.util.List;

public class FeatureCardAdapter extends RecyclerView.Adapter<FeatureCardAdapter.FeatureViewHolder> {
    private final List<FeatureCard> items;

    public FeatureCardAdapter(List<FeatureCard> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeatureCardBinding binding = ItemFeatureCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FeatureViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FeatureViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeatureCardBinding binding;

        FeatureViewHolder(ItemFeatureCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FeatureCard item) {
            binding.iconImage.setImageResource(item.getIconResId());
            binding.titleText.setText(item.getTitle());
            binding.descriptionText.setText(item.getDescription());
            binding.statusText.setVisibility(item.isAvailableSoon() ? View.VISIBLE : View.GONE);
            binding.getRoot().setAlpha(item.isAvailableSoon() ? 0.78f : 1f);
        }
    }
}
