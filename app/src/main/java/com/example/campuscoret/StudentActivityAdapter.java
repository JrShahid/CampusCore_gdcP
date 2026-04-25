package com.example.campuscoret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentActivityAdapter extends RecyclerView.Adapter<StudentActivityAdapter.StudentActivityViewHolder> {
    private final List<StudentActivityEvent> items = new ArrayList<>();

    public void submitList(List<StudentActivityEvent> events) {
        items.clear();
        items.addAll(events);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_activity, parent, false);
        return new StudentActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentActivityViewHolder holder, int position) {
        StudentActivityEvent event = items.get(position);
        holder.name.setText(event.getStudentName());
        holder.meta.setText(event.getClassName() + " - " + event.getActionLabel());
        holder.time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(event.getOccurredAtMillis())));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudentActivityViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView meta;
        private final TextView time;

        StudentActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.monitor_activity_name);
            meta = itemView.findViewById(R.id.monitor_activity_meta);
            time = itemView.findViewById(R.id.monitor_activity_time);
        }
    }
}
