package com.example.campuscoret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MetadataAdapter extends RecyclerView.Adapter<MetadataAdapter.MetadataViewHolder> {
    public interface OnDeleteListener {
        void onDelete(String item);
    }

    private final List<String> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public MetadataAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void submitList(List<String> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MetadataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_metadata, parent, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetadataViewHolder holder, int position) {
        String item = items.get(position);
        holder.nameText.setText(item);
        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MetadataViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final View deleteButton;

        MetadataViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.metadata_name);
            deleteButton = itemView.findViewById(R.id.delete_metadata_button);
        }
    }
}
