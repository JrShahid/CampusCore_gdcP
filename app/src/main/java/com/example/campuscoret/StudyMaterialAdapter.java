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

public class StudyMaterialAdapter extends RecyclerView.Adapter<StudyMaterialAdapter.StudyMaterialViewHolder> {
    public interface MaterialActionListener {
        void onView(StudyMaterial material);
        void onDownload(StudyMaterial material);
    }

    private final List<StudyMaterial> items = new ArrayList<>();
    private final MaterialActionListener listener;

    public StudyMaterialAdapter(MaterialActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StudyMaterial> materials) {
        items.clear();
        items.addAll(materials);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudyMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_material, parent, false);
        return new StudyMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyMaterialViewHolder holder, int position) {
        StudyMaterial material = items.get(position);
        holder.title.setText(material.getTitle());
        holder.meta.setText(material.getSubjectName() + " • " + material.getClassName() + " • " + material.getFileType());
        holder.fileName.setText(material.getFileName());
        holder.uploadedAt.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(material.getUploadedAtMillis())));
        holder.viewButton.setOnClickListener(v -> listener.onView(material));
        holder.downloadButton.setOnClickListener(v -> listener.onDownload(material));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StudyMaterialViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;
        private final TextView fileName;
        private final TextView uploadedAt;
        private final Button viewButton;
        private final Button downloadButton;

        StudyMaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.material_title);
            meta = itemView.findViewById(R.id.material_meta);
            fileName = itemView.findViewById(R.id.material_file_name);
            uploadedAt = itemView.findViewById(R.id.material_uploaded_at);
            viewButton = itemView.findViewById(R.id.material_view_button);
            downloadButton = itemView.findViewById(R.id.material_download_button);
        }
    }
}
