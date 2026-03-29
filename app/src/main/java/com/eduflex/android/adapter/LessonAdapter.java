package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.model.Lesson;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {

    private final List<Lesson> lessons;

    public LessonAdapter(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.tvLessonOrder.setText("Lesson " + (position + 1));
        holder.tvTitle.setText(lesson.getTitle());
        holder.tvContentType.setText(lesson.getContentType());
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonOrder;
        TextView tvTitle;
        TextView tvContentType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonOrder = itemView.findViewById(R.id.tv_lesson_order);
            tvTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvContentType = itemView.findViewById(R.id.tv_content_type);
        }
    }
}
