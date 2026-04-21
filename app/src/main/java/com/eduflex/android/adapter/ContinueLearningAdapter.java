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

public class ContinueLearningAdapter extends RecyclerView.Adapter<ContinueLearningAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(EnrolledCourse course);
    }

    private final List<EnrolledCourse> items;
    private final OnCourseClickListener listener;

    public ContinueLearningAdapter(List<EnrolledCourse> items, OnCourseClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_continue_learning, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrolledCourse course = items.get(position);
        holder.tvTitle.setText(course.getTitle());

        int progressPercent = (int) Math.round(course.getProgressPercent());
        holder.progressBar.setProgress(progressPercent);
        holder.tvPercent.setText(progressPercent + "% complete");
        holder.tvLessonCount.setText(course.getLearningMode() != null ? course.getLearningMode() : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCourseClick(course);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvLessonCount;
        TextView tvPercent;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_course_title);
            tvLessonCount = itemView.findViewById(R.id.tv_lesson_count);
            tvPercent = itemView.findViewById(R.id.tv_percent);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}