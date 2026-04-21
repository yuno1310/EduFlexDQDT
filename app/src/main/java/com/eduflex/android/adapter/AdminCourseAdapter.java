package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.Course;

import java.util.ArrayList;
import java.util.List;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.ViewHolder> {

    private final List<Course> allCourses;
    private final List<Course> displayedCourses;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Course course, int position);
    }

    public AdminCourseAdapter(List<Course> courses, OnDeleteClickListener deleteListener) {
        this.allCourses = new ArrayList<>(courses);
        this.displayedCourses = new ArrayList<>(courses);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = displayedCourses.get(position);

        holder.tvCourseTitle.setText(course.getTitle());
        holder.tvCourseModel.setText("Mode: " + (course.getLearningMode() != null ? course.getLearningMode() : "N/A"));
        holder.tvCourseStatus.setText("Status: " + (course.getStatus() != null ? course.getStatus() : "Active"));

        if (course.getTitle() != null && !course.getTitle().isEmpty()) {
            holder.tvCourseInitial.setText(course.getTitle().substring(0, 1).toUpperCase());
        } else {
            holder.tvCourseInitial.setText("C");
        }

        holder.btnDeleteCourse.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(course, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedCourses.size();
    }

    public void removeCourse(int position) {
        if (position >= 0 && position < displayedCourses.size()) {
            Course removedCourse = displayedCourses.remove(position);
            allCourses.remove(removedCourse);
            notifyItemRemoved(position);
        }
    }

    public void filter(String query) {
        displayedCourses.clear();
        if (query.isEmpty()) {
            displayedCourses.addAll(allCourses);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Course course : allCourses) {
                boolean matchTitle = course.getTitle() != null && course.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchModel = course.getLearningMode() != null && course.getLearningMode().toLowerCase().contains(lowerQuery);
                if (matchTitle || matchModel) {
                    displayedCourses.add(course);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseInitial;
        TextView tvCourseTitle;
        TextView tvCourseModel;
        TextView tvCourseStatus;
        ImageButton btnDeleteCourse;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseInitial = itemView.findViewById(R.id.tv_course_initial);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseModel = itemView.findViewById(R.id.tv_course_model);
            tvCourseStatus = itemView.findViewById(R.id.tv_course_status);
            btnDeleteCourse = itemView.findViewById(R.id.btn_delete_course);
        }
    }
}
