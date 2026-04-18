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

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
    private final List<AttendanceRecord> items = new ArrayList<>();

    public void submitList(List<AttendanceRecord> records) {
        items.clear();
        items.addAll(records);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_record, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = items.get(position);
        holder.studentName.setText(record.getStudentName());
        holder.studentEmail.setText(record.getStudentEmail());
        holder.status.setText(record.getStatus());
        holder.markedAt.setText(
                DateFormat.getTimeInstance(DateFormat.SHORT)
                        .format(new Date(record.getMarkedAtMillis()))
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentName;
        private final TextView studentEmail;
        private final TextView status;
        private final TextView markedAt;

        AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.attendance_student_name);
            studentEmail = itemView.findViewById(R.id.attendance_student_email);
            status = itemView.findViewById(R.id.attendance_status);
            markedAt = itemView.findViewById(R.id.attendance_marked_at);
        }
    }
}
