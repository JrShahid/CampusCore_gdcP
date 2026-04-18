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

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder> {
    public interface SubmissionActionListener {
        void onEvaluate(AssignmentSubmission submission);
    }

    private final List<AssignmentSubmission> items = new ArrayList<>();
    private final SubmissionActionListener listener;

    public SubmissionAdapter(SubmissionActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AssignmentSubmission> submissions) {
        items.clear();
        items.addAll(submissions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);
        return new SubmissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        AssignmentSubmission submission = items.get(position);
        holder.studentName.setText(submission.getStudentName());
        holder.solutionText.setText(submission.getSolutionText());
        holder.fileUrl.setText(submission.getFileUrl());
        holder.submittedAt.setText(
                holder.itemView.getContext().getString(
                        R.string.assignment_submitted_at_label,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(new Date(submission.getSubmittedAtMillis()))
                )
        );
        if (submission.getMarks() == null) {
            holder.evaluation.setText(R.string.assignment_not_evaluated);
        } else {
            holder.evaluation.setText(
                    holder.itemView.getContext().getString(
                            R.string.assignment_marks_feedback,
                            submission.getMarks(),
                            submission.getFeedback()
                    )
            );
        }
        holder.evaluateButton.setOnClickListener(v -> listener.onEvaluate(submission));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubmissionViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentName;
        private final TextView solutionText;
        private final TextView fileUrl;
        private final TextView submittedAt;
        private final TextView evaluation;
        private final Button evaluateButton;

        SubmissionViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.submission_student_name);
            solutionText = itemView.findViewById(R.id.submission_solution_text);
            fileUrl = itemView.findViewById(R.id.submission_file_url);
            submittedAt = itemView.findViewById(R.id.submission_submitted_at);
            evaluation = itemView.findViewById(R.id.submission_evaluation);
            evaluateButton = itemView.findViewById(R.id.submission_evaluate_button);
        }
    }
}
