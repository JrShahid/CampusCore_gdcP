package com.example.campuscore.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuscore.R;
import com.example.campuscore.databinding.ItemSubjectAttendanceSummaryBinding;
import com.example.campuscore.models.SubjectAttendanceSummary;

import java.util.List;
import java.util.Locale;

public class SubjectAttendanceSummaryAdapter extends RecyclerView.Adapter<SubjectAttendanceSummaryAdapter.SubjectSummaryViewHolder> {
    private final List<SubjectAttendanceSummary> items;

    public SubjectAttendanceSummaryAdapter(List<SubjectAttendanceSummary> items) {
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public SubjectSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectAttendanceSummaryBinding binding = ItemSubjectAttendanceSummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SubjectSummaryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectSummaryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getSubject().hashCode();
    }

    public void submitList(List<SubjectAttendanceSummary> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    static class SubjectSummaryViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectAttendanceSummaryBinding binding;

        SubjectSummaryViewHolder(ItemSubjectAttendanceSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SubjectAttendanceSummary item) {
            binding.subjectText.setText(item.getSubject());
            binding.ratioText.setText(binding.getRoot().getContext().getString(
                    R.string.subject_summary_ratio,
                    item.getPresentCount(),
                    item.getTotalCount()
            ));
            binding.progressIndicator.setProgress((int) Math.round(item.getPercentage()));
            binding.percentageText.setText(String.format(Locale.getDefault(), "%.1f%%", item.getPercentage()));
        }
    }

    private static final class DiffCallback extends DiffUtil.Callback {
        private final List<SubjectAttendanceSummary> oldItems;
        private final List<SubjectAttendanceSummary> newItems;

        private DiffCallback(List<SubjectAttendanceSummary> oldItems, List<SubjectAttendanceSummary> newItems) {
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
            return oldItems.get(oldItemPosition).getSubject()
                    .equals(newItems.get(newItemPosition).getSubject());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            SubjectAttendanceSummary oldItem = oldItems.get(oldItemPosition);
            SubjectAttendanceSummary newItem = newItems.get(newItemPosition);
            return oldItem.getPresentCount() == newItem.getPresentCount()
                    && oldItem.getTotalCount() == newItem.getTotalCount()
                    && Double.compare(oldItem.getPercentage(), newItem.getPercentage()) == 0;
        }
    }
}
