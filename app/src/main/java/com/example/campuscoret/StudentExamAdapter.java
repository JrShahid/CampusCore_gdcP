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

public class StudentExamAdapter extends RecyclerView.Adapter<StudentExamAdapter.StudentExamViewHolder> {
    public interface StudentExamActionListener {
        void onPrimaryAction(ExamRecord exam);
        void onSecondaryAction(ExamRecord exam);
    }

    private final List<ExamRecord> items = new ArrayList<>();
    private final StudentExamActionListener listener;
    private final String studentId;

    public StudentExamAdapter(String studentId, StudentExamActionListener listener) {
        this.studentId = studentId;
        this.listener = listener;
    }

    public void submitList(List<ExamRecord> exams) {
        items.clear();
        items.addAll(exams);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new StudentExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentExamViewHolder holder, int position) {
        ExamRecord exam = items.get(position);
        ExamAttempt attempt = ExamRepository.getAttemptForStudent(exam.getExamId(), studentId);
        holder.title.setText(exam.getTitle());
        holder.meta.setText(exam.getSubjectName() + " - " + exam.getClassName());
        holder.schedule.setText(holder.itemView.getContext().getString(
                R.string.exam_schedule_label,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(exam.getStartTimeMillis())),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(exam.getEndTimeMillis()))
        ));

        if (attempt == null) {
            holder.status.setText(holder.itemView.getContext().getString(
                    R.string.exam_student_status_summary,
                    exam.getDurationMinutes(),
                    exam.getTotalMarks()
            ));
            holder.primaryButton.setText(R.string.exam_start_button);
            holder.primaryButton.setEnabled(true);
            holder.secondaryButton.setText(R.string.exam_result_button);
            holder.secondaryButton.setEnabled(false);
        } else {
            holder.status.setText(holder.itemView.getContext().getString(
                    R.string.exam_result_summary,
                    attempt.getScore(),
                    exam.getTotalMarks(),
                    attempt.getPercentage()
            ));
            holder.primaryButton.setText(R.string.exam_completed_button);
            holder.primaryButton.setEnabled(false);
            holder.secondaryButton.setText(R.string.exam_result_button);
            holder.secondaryButton.setEnabled(true);
        }

        holder.primaryButton.setOnClickListener(v -> listener.onPrimaryAction(exam));
        holder.secondaryButton.setOnClickListener(v -> listener.onSecondaryAction(exam));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudentExamViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;
        private final TextView schedule;
        private final TextView status;
        private final Button primaryButton;
        private final Button secondaryButton;

        StudentExamViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.exam_title);
            meta = itemView.findViewById(R.id.exam_meta);
            schedule = itemView.findViewById(R.id.exam_schedule);
            status = itemView.findViewById(R.id.exam_status);
            primaryButton = itemView.findViewById(R.id.exam_primary_button);
            secondaryButton = itemView.findViewById(R.id.exam_secondary_button);
        }
    }
}
