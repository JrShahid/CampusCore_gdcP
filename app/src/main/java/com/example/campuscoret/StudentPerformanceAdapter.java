package com.example.campuscoret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentPerformanceAdapter extends RecyclerView.Adapter<StudentPerformanceAdapter.StudentPerformanceViewHolder> {
    public interface StudentPerformanceListener {
        void onStudentSelected(StudentPerformanceSummary summary);
    }

    private final List<StudentPerformanceSummary> items = new ArrayList<>();
    private final StudentPerformanceListener listener;

    public StudentPerformanceAdapter(StudentPerformanceListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StudentPerformanceSummary> summaries) {
        items.clear();
        items.addAll(summaries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentPerformanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_performance, parent, false);
        return new StudentPerformanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentPerformanceViewHolder holder, int position) {
        StudentPerformanceSummary summary = items.get(position);
        holder.name.setText(summary.getStudentName());
        holder.meta.setText(summary.getStudentId());
        holder.score.setText(holder.itemView.getContext().getString(
                R.string.performance_final_score,
                String.format(Locale.getDefault(), "%.2f", summary.getFinalScore()),
                summary.getStatus()
        ));
        holder.breakdown.setText(holder.itemView.getContext().getString(
                R.string.performance_breakdown_line,
                summary.getAttendancePercentage(),
                summary.getAssignmentPercentage(),
                summary.getExamPercentage(),
                summary.getBehaviorPercentage()
        ));
        holder.support.setText(summary.getSupportMessage());
        holder.itemView.setOnClickListener(v -> listener.onStudentSelected(summary));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudentPerformanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView meta;
        private final TextView score;
        private final TextView breakdown;
        private final TextView support;

        StudentPerformanceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.performance_student_name);
            meta = itemView.findViewById(R.id.performance_student_meta);
            score = itemView.findViewById(R.id.performance_student_score);
            breakdown = itemView.findViewById(R.id.performance_student_breakdown);
            support = itemView.findViewById(R.id.performance_student_support);
        }
    }
}
