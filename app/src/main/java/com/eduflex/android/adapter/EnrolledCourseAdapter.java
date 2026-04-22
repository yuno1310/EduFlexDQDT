package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.EnrolledCourse;

import java.util.List;

public class EnrolledCourseAdapter extends RecyclerView.Adapter<EnrolledCourseAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(EnrolledCourse course);
    }

    private final List<EnrolledCourse> items;
    private final OnCourseClickListener listener;

    public EnrolledCourseAdapter(List<EnrolledCourse> items, OnCourseClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_enrolled_course, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrolledCourse course = items.get(position);
        holder.tvTitle.setText(course.getTitle());
        String mode = course.getLearningMode();
        holder.tvMode.setText(mode != null && !mode.isEmpty() ? mode : "Self-paced");
        int progress = (int) course.getProgressPercent();
        holder.progressBar.setProgress(progress);
        holder.tvProgressLabel.setText(progress + "% complete");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMode, tvProgressLabel;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_enrolled_title);
            tvMode = itemView.findViewById(R.id.tv_enrolled_mode);
            progressBar = itemView.findViewById(R.id.pb_course_progress);
            tvProgressLabel = itemView.findViewById(R.id.tv_progress_label);
        }
    }
}
