package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.Lesson;

import java.util.List;

public class AdminLessonAdapter extends RecyclerView.Adapter<AdminLessonAdapter.ViewHolder> {

    private final List<Lesson> lessons;
    private final OnEditClickListener editListener;
    private final OnQuizEditClickListener quizEditListener;
    private final OnDeleteClickListener deleteListener;

    public interface OnEditClickListener {
        void onEditClick(Lesson lesson, int position);
    }

    public interface OnQuizEditClickListener {
        void onQuizEditClick(Lesson lesson);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Lesson lesson, int position);
    }

    public AdminLessonAdapter(List<Lesson> lessons,
                              OnEditClickListener editListener,
                              OnQuizEditClickListener quizEditListener,
                              OnDeleteClickListener deleteListener) {
        this.lessons = lessons;
        this.editListener = editListener;
        this.quizEditListener = quizEditListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);

        holder.tvLessonNumber.setText(String.valueOf(position + 1));
        holder.tvLessonTitle.setText(lesson.getTitle());
        holder.tvLessonType.setText(lesson.getContentType() != null ? lesson.getContentType() : "reading");

        // Show quiz edit button for all lessons — API will respond if quiz exists
        holder.btnEditQuiz.setVisibility(View.VISIBLE);

        holder.btnEditLesson.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(lesson, position);
            }
        });

        holder.btnEditQuiz.setOnClickListener(v -> {
            if (quizEditListener != null) {
                quizEditListener.onQuizEditClick(lesson);
            }
        });

        holder.btnDeleteLesson.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(lesson, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void updateLesson(int position, Lesson updated) {
        if (position >= 0 && position < lessons.size()) {
            lessons.set(position, updated);
            notifyItemChanged(position);
        }
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLessonNumber;
        TextView tvLessonTitle;
        TextView tvLessonType;
        ImageButton btnEditLesson;
        ImageButton btnEditQuiz;
        ImageButton btnDeleteLesson;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonNumber = itemView.findViewById(R.id.tv_lesson_number);
            tvLessonTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvLessonType = itemView.findViewById(R.id.tv_lesson_type);
            btnEditLesson = itemView.findViewById(R.id.btn_edit_lesson);
            btnEditQuiz = itemView.findViewById(R.id.btn_edit_quiz);
            btnDeleteLesson = itemView.findViewById(R.id.btn_delete_lesson);
        }
    }
}
