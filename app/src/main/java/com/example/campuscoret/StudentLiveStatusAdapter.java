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

public class StudentLiveStatusAdapter extends RecyclerView.Adapter<StudentLiveStatusAdapter.StudentLiveStatusViewHolder> {
    private final List<StudentLiveStatus> items = new ArrayList<>();

    public void submitList(List<StudentLiveStatus> statuses) {
        items.clear();
        items.addAll(statuses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentLiveStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_status, parent, false);
        return new StudentLiveStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentLiveStatusViewHolder holder, int position) {
        StudentLiveStatus status = items.get(position);
        holder.name.setText(status.getStudentName());
        holder.meta.setText(status.getClassName() + " - " + (status.isOnline() ? "Online" : "Offline"));
        holder.action.setText(status.getLastAction());
        holder.time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(status.getLastActiveMillis())));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudentLiveStatusViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView meta;
        private final TextView action;
        private final TextView time;

        StudentLiveStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.monitor_status_name);
            meta = itemView.findViewById(R.id.monitor_status_meta);
            action = itemView.findViewById(R.id.monitor_status_action);
            time = itemView.findViewById(R.id.monitor_status_time);
        }
    }
}
