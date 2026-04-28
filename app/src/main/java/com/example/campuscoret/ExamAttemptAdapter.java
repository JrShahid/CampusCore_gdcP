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

public class ExamAttemptAdapter extends RecyclerView.Adapter<ExamAttemptAdapter.ExamAttemptViewHolder> {
    private final List<ExamAttempt> items = new ArrayList<>();

    public void submitList(List<ExamAttempt> attempts) {
        items.clear();
        items.addAll(attempts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamAttemptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam_attempt, parent, false);
        return new ExamAttemptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamAttemptViewHolder holder, int position) {
        ExamAttempt attempt = items.get(position);
        holder.studentName.setText(attempt.getStudentName());
        holder.studentId.setText(attempt.getStudentId());
        holder.score.setText(holder.itemView.getContext().getString(
                R.string.exam_attempt_score,
                attempt.getScore(),
                attempt.getPercentage()
        ));
        holder.feedback.setText(attempt.getFeedback());
        holder.submittedAt.setText(holder.itemView.getContext().getString(
                R.string.exam_attempt_submitted_at,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(new Date(attempt.getSubmittedAtMillis()))
        ));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ExamAttemptViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentName;
        private final TextView studentId;
        private final TextView score;
        private final TextView feedback;
        private final TextView submittedAt;

        ExamAttemptViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.exam_attempt_student_name);
            studentId = itemView.findViewById(R.id.exam_attempt_student_id);
            score = itemView.findViewById(R.id.exam_attempt_score);
            feedback = itemView.findViewById(R.id.exam_attempt_feedback);
            submittedAt = itemView.findViewById(R.id.exam_attempt_submitted_at);
        }
    }
}
