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

public class TeacherExamAdapter extends RecyclerView.Adapter<TeacherExamAdapter.TeacherExamViewHolder> {
    public interface TeacherExamActionListener {
        void onPrimaryAction(ExamRecord exam);
        void onSecondaryAction(ExamRecord exam);
    }

    private final List<ExamRecord> items = new ArrayList<>();
    private final TeacherExamActionListener listener;

    public TeacherExamAdapter(TeacherExamActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ExamRecord> exams) {
        items.clear();
        items.addAll(exams);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeacherExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new TeacherExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherExamViewHolder holder, int position) {
        ExamRecord exam = items.get(position);
        long now = System.currentTimeMillis();
        holder.title.setText(exam.getTitle());
        holder.meta.setText(exam.getSubjectName() + " - " + exam.getClassName());
        holder.schedule.setText(holder.itemView.getContext().getString(
                R.string.exam_schedule_label,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(exam.getStartTimeMillis())),
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(exam.getEndTimeMillis()))
        ));
        holder.status.setText(holder.itemView.getContext().getString(
                R.string.exam_teacher_status_summary,
                resolveStatusLabel(holder.itemView, exam, now),
                exam.getQuestions().size(),
                ExamRepository.getAttemptCount(exam.getExamId())
        ));
        holder.primaryButton.setText(R.string.exam_performance_button);
        holder.secondaryButton.setText(R.string.exam_questions_button);
        holder.primaryButton.setOnClickListener(v -> listener.onPrimaryAction(exam));
        holder.secondaryButton.setOnClickListener(v -> listener.onSecondaryAction(exam));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String resolveStatusLabel(View view, ExamRecord exam, long now) {
        if (ExamRepository.isExamActive(exam, now)) {
            return view.getContext().getString(R.string.exam_status_active);
        }
        if (ExamRepository.isExamLocked(exam, now)) {
            return view.getContext().getString(R.string.exam_status_locked);
        }
        return view.getContext().getString(R.string.exam_status_scheduled);
    }

    static class TeacherExamViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;
        private final TextView schedule;
        private final TextView status;
        private final Button primaryButton;
        private final Button secondaryButton;

        TeacherExamViewHolder(@NonNull View itemView) {
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
