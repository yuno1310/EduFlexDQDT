package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.eduflex.android.R;
import com.eduflex.android.model.Course;

import java.util.ArrayList;
import java.util.List;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.ViewHolder> {

    private final List<Course> allCourses;
    private final List<Course> displayedCourses;
    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener editListener;
    private final OnItemClickListener itemClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Course course, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(Course course, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(Course course);
    }

    public AdminCourseAdapter(List<Course> courses,
                              OnDeleteClickListener deleteListener,
                              OnEditClickListener editListener,
                              OnItemClickListener itemClickListener) {
        this.allCourses = new ArrayList<>(courses);
        this.displayedCourses = new ArrayList<>(courses);
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.itemClickListener = itemClickListener;
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

        // Load course image or show initial letter
        String imageUrl = course.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.ivCourseImage.setVisibility(View.VISIBLE);
            holder.flCourseInitial.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new CircleCrop())
                    .into(holder.ivCourseImage);
        } else {
            holder.ivCourseImage.setVisibility(View.GONE);
            holder.flCourseInitial.setVisibility(View.VISIBLE);
            if (course.getTitle() != null && !course.getTitle().isEmpty()) {
                holder.tvCourseInitial.setText(course.getTitle().substring(0, 1).toUpperCase());
            } else {
                holder.tvCourseInitial.setText("C");
            }
        }

        holder.btnDeleteCourse.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(course, position);
            }
        });

        holder.btnEditCourse.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(course, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(course);
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

    public void updateCourse(int position, Course updated) {
        if (position >= 0 && position < displayedCourses.size()) {
            displayedCourses.set(position, updated);
            for (int i = 0; i < allCourses.size(); i++) {
                if (allCourses.get(i).getCourseID().equals(updated.getCourseID())) {
                    allCourses.set(i, updated);
                    break;
                }
            }
            notifyItemChanged(position);
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
        FrameLayout flCourseInitial;
        TextView tvCourseInitial;
        ImageView ivCourseImage;
        TextView tvCourseTitle;
        TextView tvCourseModel;
        TextView tvCourseStatus;
        ImageButton btnEditCourse;
        ImageButton btnDeleteCourse;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            flCourseInitial = itemView.findViewById(R.id.fl_course_initial);
            tvCourseInitial = itemView.findViewById(R.id.tv_course_initial);
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseModel = itemView.findViewById(R.id.tv_course_model);
            tvCourseStatus = itemView.findViewById(R.id.tv_course_status);
            btnEditCourse = itemView.findViewById(R.id.btn_edit_course);
            btnDeleteCourse = itemView.findViewById(R.id.btn_delete_course);
        }
    }
}
