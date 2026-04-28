package com.example.campuscoret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {
    public interface AssignmentActionListener {
        void onPrimaryAction(Assignment assignment);
        void onSecondaryAction(Assignment assignment);
    }

    private final List<Assignment> items = new ArrayList<>();
    private final AssignmentActionListener listener;
    private final int primaryTextResId;
    private final int secondaryTextResId;

    public AssignmentAdapter(AssignmentActionListener listener, int primaryTextResId, int secondaryTextResId) {
        this.listener = listener;
        this.primaryTextResId = primaryTextResId;
        this.secondaryTextResId = secondaryTextResId;
    }

    public void submitList(List<Assignment> assignments) {
        items.clear();
        items.addAll(assignments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = items.get(position);
        holder.title.setText(assignment.getTitle());
        holder.meta.setText(assignment.getSubjectName() + " • " + assignment.getClassName());
        holder.description.setText(assignment.getDescription());
        holder.deadline.setText(
                holder.itemView.getContext().getString(
                        R.string.assignment_deadline_label,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(new Date(assignment.getDeadlineMillis()))
                )
        );
        holder.primaryButton.setText(primaryTextResId);
        holder.secondaryButton.setText(secondaryTextResId);
        holder.primaryButton.setOnClickListener(v -> listener.onPrimaryAction(assignment));
        holder.secondaryButton.setOnClickListener(v -> listener.onSecondaryAction(assignment));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;
        private final TextView description;
        private final TextView deadline;
        private final Button primaryButton;
        private final Button secondaryButton;

        AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.assignment_title);
            meta = itemView.findViewById(R.id.assignment_meta);
            description = itemView.findViewById(R.id.assignment_description);
            deadline = itemView.findViewById(R.id.assignment_deadline);
            primaryButton = itemView.findViewById(R.id.assignment_primary_button);
            secondaryButton = itemView.findViewById(R.id.assignment_secondary_button);
        }
    }
}
