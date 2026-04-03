package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.model.Course;
import java.util.List;

public class ContinueLearningAdapter extends RecyclerView.Adapter<ContinueLearningAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<Course> items;
    private final OnCourseClickListener listener;

    public ContinueLearningAdapter(List<Course> items, OnCourseClickListener listener) {
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
        Course course = items.get(position);
        holder.tvTitle.setText(course.getTitle());

        int completedLessons = (position % 10) + 1;
        int progressPercent = completedLessons * 10;
        String status = course.getStatus();
        if (status != null && "completed".equalsIgnoreCase(status)) {
            completedLessons = 10;
            progressPercent = 100;
        }

        holder.tvLessonCount.setText("Lesson " + completedLessons + " / 10");
        holder.progressBar.setProgress(progressPercent);
        holder.tvPercent.setText(progressPercent + "% complete");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourseClick(course);
            }
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