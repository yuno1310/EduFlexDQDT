package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.eduflex.android.R;
import com.eduflex.android.model.Course;
import java.util.List;

public class CourseCardAdapter extends RecyclerView.Adapter<CourseCardAdapter.ViewHolder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final List<Course> items;
    private final OnCourseClickListener listener;
    private boolean showStatus = true;

    public CourseCardAdapter(List<Course> items, OnCourseClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = items.get(position);
        holder.tvTitle.setText(course.getTitle());
        holder.tvInstructor.setText(course.getLearningMode());
        holder.tvInstructor.setVisibility(course.getLearningMode() == null
                || course.getLearningMode().trim().isEmpty() ? View.GONE : View.VISIBLE);
        if (showStatus) {
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText(course.getStatus());
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }
        Glide.with(holder.itemView.getContext())
                .load(course.getImageUrl())
                .placeholder(R.drawable.bg_course_placeholder)
                .error(R.drawable.bg_course_placeholder)
                .centerCrop()
                .into(holder.ivThumbnail);
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
        TextView tvInstructor;
        TextView tvPrice;
        ImageView ivThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvInstructor = itemView.findViewById(R.id.tv_instructor);
            tvPrice = itemView.findViewById(R.id.tv_price);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
        }
    }
}
