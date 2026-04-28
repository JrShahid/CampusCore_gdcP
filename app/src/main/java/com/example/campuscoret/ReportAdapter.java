package com.example.campuscoret;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private final List<ReportRow> items = new ArrayList<>();

    public void submitList(List<ReportRow> rows) {
        items.clear();
        items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportRow row = items.get(position);
        holder.title.setText(row.getTitle());
        holder.lineOne.setText(row.getLineOne());
        holder.lineTwo.setText(row.getLineTwo());
        holder.lineThree.setText(row.getLineThree());
        holder.lineThree.setVisibility(row.getLineThree().isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView lineOne;
        private final TextView lineTwo;
        private final TextView lineThree;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.report_row_title);
            lineOne = itemView.findViewById(R.id.report_row_line_one);
            lineTwo = itemView.findViewById(R.id.report_row_line_two);
            lineThree = itemView.findViewById(R.id.report_row_line_three);
        }
    }
}
