package com.example.campuscore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuscore.R;
import com.example.campuscore.databinding.ItemAttendanceHistoryBinding;
import com.example.campuscore.models.AttendanceModel;
import com.example.campuscore.utils.AttendanceConstants;

import java.util.List;

public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.AttendanceHistoryViewHolder> {
    private final List<AttendanceModel> items;
    private final boolean showStudentDetails;

    public AttendanceHistoryAdapter(List<AttendanceModel> items, boolean showStudentDetails) {
        this.items = items;
        this.showStudentDetails = showStudentDetails;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public AttendanceHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceHistoryBinding binding = ItemAttendanceHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AttendanceHistoryViewHolder(binding, showStudentDetails);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceHistoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getAttendanceId().hashCode();
    }

    public void submitList(List<AttendanceModel> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    static class AttendanceHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceHistoryBinding binding;
        private final boolean showStudentDetails;

        AttendanceHistoryViewHolder(ItemAttendanceHistoryBinding binding, boolean showStudentDetails) {
            super(binding.getRoot());
            this.binding = binding;
            this.showStudentDetails = showStudentDetails;
        }

        void bind(AttendanceModel item) {
            binding.titleText.setText(item.getSubject());
            if (showStudentDetails) {
                String subtitle = item.getDate() + " - " + item.getStudentName()
                        + (item.getRollNumber().isEmpty() ? "" : " - " + item.getRollNumber());
                binding.subtitleText.setText(subtitle);
            } else {
                binding.subtitleText.setText(item.getDate());
            }

            binding.statusText.setText(item.getStatus());
            if (AttendanceConstants.STATUS_PRESENT.equalsIgnoreCase(item.getStatus())) {
                binding.statusText.setBackgroundResource(R.drawable.bg_success_chip);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.success_green));
            } else {
                binding.statusText.setBackgroundResource(R.drawable.bg_danger_chip);
                binding.statusText.setTextColor(binding.getRoot().getContext().getColor(R.color.danger_red));
            }
        }
    }

    private static final class DiffCallback extends DiffUtil.Callback {
        private final List<AttendanceModel> oldItems;
        private final List<AttendanceModel> newItems;

        private DiffCallback(List<AttendanceModel> oldItems, List<AttendanceModel> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition).getAttendanceId()
                    .equals(newItems.get(newItemPosition).getAttendanceId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AttendanceModel oldItem = oldItems.get(oldItemPosition);
            AttendanceModel newItem = newItems.get(newItemPosition);
            return oldItem.getSubject().equals(newItem.getSubject())
                    && oldItem.getDate().equals(newItem.getDate())
                    && oldItem.getStatus().equals(newItem.getStatus())
                    && oldItem.getStudentName().equals(newItem.getStudentName())
                    && oldItem.getRollNumber().equals(newItem.getRollNumber());
        }
    }
}
