package com.eduflex.android.ui.review;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CourseReviewFragment extends Fragment {

    private static final String PREF_COURSE_REVIEW = "course_reviews";

    private String courseId;
    private String courseTitle;

    public CourseReviewFragment() {
        super(R.layout.fragment_course_review);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        courseId = args != null ? args.getString("courseId", "") : "";
        courseTitle = args != null ? args.getString("courseTitle", "Course") : "Course";

        TextView tvTitle = view.findViewById(R.id.tv_review_course_title);
        RatingBar ratingBar = view.findViewById(R.id.rating_course);
        EditText etComment = view.findViewById(R.id.et_review_comment);
        TextView tvSavedReview = view.findViewById(R.id.tv_saved_review);
        Button btnSubmit = view.findViewById(R.id.btn_submit_review);
        Button btnBack = view.findViewById(R.id.btn_back_review);

        tvTitle.setText(courseTitle);
        bindSavedReview(ratingBar, etComment, tvSavedReview);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText() == null ? "" : etComment.getText().toString().trim();

            if (rating < 1f) {
                Toast.makeText(requireContext(), "Please select a rating.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your comment.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveReview(rating, comment);
            bindSavedReview(ratingBar, etComment, tvSavedReview);
            Toast.makeText(requireContext(), "Review submitted.", Toast.LENGTH_SHORT).show();
        });
    }

    private void bindSavedReview(RatingBar ratingBar, EditText etComment, TextView tvSavedReview) {
        if (courseId == null || courseId.isEmpty()) {
            tvSavedReview.setText("No course selected.");
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_COURSE_REVIEW, Context.MODE_PRIVATE);
        float savedRating = prefs.getFloat(courseId + "_rating", 0f);
        String savedComment = prefs.getString(courseId + "_comment", "");
        String savedTime = prefs.getString(courseId + "_time", "");

        if (savedRating > 0f) {
            ratingBar.setRating(savedRating);
        }
        if (!savedComment.isEmpty()) {
            etComment.setText(savedComment);
        }

        if (!savedComment.isEmpty() && !savedTime.isEmpty()) {
            tvSavedReview.setText(String.format(
                    Locale.getDefault(),
                    "Last review: %.1f★ • %s\n%s",
                    savedRating,
                    savedTime,
                    savedComment
            ));
        } else {
            tvSavedReview.setText("No review yet.");
        }
    }

    private void saveReview(float rating, String comment) {
        if (courseId == null || courseId.isEmpty()) {
            return;
        }
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_COURSE_REVIEW, Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat(courseId + "_rating", rating)
                .putString(courseId + "_comment", comment)
                .putString(courseId + "_time", time)
                .apply();
    }
}
