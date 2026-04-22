package com.eduflex.android.ui.review;

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
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseReviewApi;
import com.eduflex.android.model.CourseReviewItem;
import com.eduflex.android.model.CourseReviewListResponse;
import com.eduflex.android.model.CourseReviewRequest;
import com.eduflex.android.model.CourseReviewSubmitResponse;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseReviewFragment extends Fragment {

    private String courseId;
    private String courseTitle;
    private CourseReviewApi courseReviewApi;

    public CourseReviewFragment() {
        super(R.layout.fragment_course_review);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        courseId = args != null ? args.getString("courseId", "") : "";
        courseTitle = args != null ? args.getString("courseTitle", "Course") : "Course";
        courseReviewApi = ApiClient.createAuthenticatedService(CourseReviewApi.class);

        TextView tvTitle = view.findViewById(R.id.tv_review_course_title);
        RatingBar ratingBar = view.findViewById(R.id.rating_course);
        EditText etComment = view.findViewById(R.id.et_review_comment);
        TextView tvSavedReview = view.findViewById(R.id.tv_saved_review);
        Button btnSubmit = view.findViewById(R.id.btn_submit_review);
        Button btnBack = view.findViewById(R.id.btn_back_review);

        tvTitle.setText(courseTitle);
        loadReviews(tvSavedReview);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        btnSubmit.setOnClickListener(v -> {
            if (courseId == null || courseId.isEmpty()) {
                Toast.makeText(requireContext(), "Invalid course.", Toast.LENGTH_SHORT).show();
                return;
            }

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

            submitReview(btnSubmit, tvSavedReview, (int) rating, comment);
        });
    }

    private void submitReview(Button btnSubmit, TextView tvSavedReview, int rating, String comment) {
        btnSubmit.setEnabled(false);
        courseReviewApi.submitReview(courseId, new CourseReviewRequest(rating, comment))
                .enqueue(new Callback<CourseReviewSubmitResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CourseReviewSubmitResponse> call,
                                           @NonNull Response<CourseReviewSubmitResponse> response) {
                        if (!isAdded()) {
                            return;
                        }
                        btnSubmit.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(requireContext(), "Review submitted.", Toast.LENGTH_SHORT).show();
                            loadReviews(tvSavedReview);
                        } else {
                            String message = response.body() != null ? response.body().getMessage() : "Failed to submit review.";
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CourseReviewSubmitResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        btnSubmit.setEnabled(true);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadReviews(TextView tvSavedReview) {
        if (courseId == null || courseId.isEmpty()) {
            tvSavedReview.setText("No course selected.");
            return;
        }

        courseReviewApi.getCourseReviews(courseId).enqueue(new Callback<CourseReviewListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CourseReviewListResponse> call,
                                   @NonNull Response<CourseReviewListResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    tvSavedReview.setText("No review yet.");
                    return;
                }

                List<CourseReviewItem> reviews = response.body().getReviews();
                if (reviews == null || reviews.isEmpty()) {
                    tvSavedReview.setText("No review yet.");
                    return;
                }

                StringBuilder builder = new StringBuilder("Recent feedback\n");
                int maxItems = Math.min(3, reviews.size());
                for (int i = 0; i < maxItems; i++) {
                    CourseReviewItem review = reviews.get(i);
                    String reviewer = review.getReviewerName() == null || review.getReviewerName().trim().isEmpty()
                            ? "Learner"
                            : review.getReviewerName().trim();
                    String time = review.getCreatedAt() == null ? "" : review.getCreatedAt();
                    String reviewComment = review.getComment() == null ? "" : review.getComment();

                    builder.append(String.format(
                            Locale.getDefault(),
                            "%d★ • %s • %s\n%s",
                            review.getRating(),
                            reviewer,
                            time,
                            reviewComment
                    ));
                    if (i < maxItems - 1) {
                        builder.append("\n\n");
                    }
                }

                tvSavedReview.setText(builder.toString());
            }

            @Override
            public void onFailure(@NonNull Call<CourseReviewListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                tvSavedReview.setText("Failed to load reviews.");
            }
        });
    }
}
