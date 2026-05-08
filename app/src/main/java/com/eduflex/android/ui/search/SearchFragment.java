package com.eduflex.android.ui.search;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.adapter.CourseCardAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.model.Course;
import com.eduflex.android.model.CourseListResponse;
import com.eduflex.android.model.CourseSearchResult;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final long DEBOUNCE_MS = 300;
    private static final long HINT_ROTATE_MS = 3000;

    private static final String[] HINT_SUGGESTIONS = {
        "IELTS Writing Task 2 tips",
        "Spring Boot dependency injection",
        "Power BI dashboard basics",
        "Clean Architecture patterns",
        "How to write an essay outline"
    };

    private List<Course> displayedCourses = new ArrayList<>();
    private CourseCardAdapter adapter;
    private CourseApi courseApi;
    private Call<List<CourseSearchResult>> pendingCall;
    private Call<CourseListResponse> allCoursesCall;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private final Handler hintHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private Runnable hintRotator;
    private int currentHintIndex = 0;
    private boolean isUserTyping = false;

    private RecyclerView rvResults;
    private TextView tvEmpty;
    private TextView tvAnimatedHint;
    private ImageView ivSparkle;
    private LinearLayout shimmerContainer;
    private AnimatorSet sparkleAnimator;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);

        EditText etSearch = view.findViewById(R.id.et_search);
        tvEmpty = view.findViewById(R.id.tv_search_empty);
        rvResults = view.findViewById(R.id.rv_search_results);
        tvAnimatedHint = view.findViewById(R.id.tv_animated_hint);
        ivSparkle = view.findViewById(R.id.iv_sparkle);
        shimmerContainer = view.findViewById(R.id.shimmer_container);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseCardAdapter(displayedCourses, course -> openCourseDetail(course));
        adapter.setShowStatus(false);
        rvResults.setAdapter(adapter);

        // Forward touch from hint overlay to EditText
        tvAnimatedHint.setOnClickListener(v -> {
            etSearch.requestFocus();
        });

        startHintRotation();
        startSparkleAnimation();
        startShimmerAnimation();
        loadAllCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    isUserTyping = false;
                    tvAnimatedHint.setVisibility(View.VISIBLE);
                    startHintRotation();
                    debounceHandler.removeCallbacks(pendingSearch);
                    cancelPending();
                    hideShimmer();
                    loadAllCourses();
                    return;
                }

                isUserTyping = true;
                tvAnimatedHint.setVisibility(View.GONE);
                stopHintRotation();

                debounceHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> {
                    showShimmer();
                    searchCourses(query);
                };
                debounceHandler.postDelayed(pendingSearch, DEBOUNCE_MS);
            }
        });
    }

    // --- Rotating hint animation ---

    private void startHintRotation() {
        stopHintRotation();
        tvAnimatedHint.setVisibility(View.VISIBLE);
        showHintAtIndex(currentHintIndex);

        hintRotator = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || isUserTyping) return;
                currentHintIndex = (currentHintIndex + 1) % HINT_SUGGESTIONS.length;
                animateHintTransition(HINT_SUGGESTIONS[currentHintIndex]);
                hintHandler.postDelayed(this, HINT_ROTATE_MS);
            }
        };
        hintHandler.postDelayed(hintRotator, HINT_ROTATE_MS);
    }

    private void stopHintRotation() {
        if (hintRotator != null) {
            hintHandler.removeCallbacks(hintRotator);
        }
    }

    private void showHintAtIndex(int index) {
        tvAnimatedHint.setText(HINT_SUGGESTIONS[index]);
        tvAnimatedHint.setAlpha(1f);
    }

    private void animateHintTransition(String newHint) {
        // Fade out, swap text, slide up + fade in
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvAnimatedHint, "alpha", 1f, 0f);
        fadeOut.setDuration(200);
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                tvAnimatedHint.setText(newHint);
                tvAnimatedHint.setTranslationY(8f);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tvAnimatedHint, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(tvAnimatedHint, "translationY", 8f, 0f);
                fadeIn.setDuration(300);
                slideUp.setDuration(300);
                AnimatorSet inSet = new AnimatorSet();
                inSet.playTogether(fadeIn, slideUp);
                inSet.setInterpolator(new AccelerateDecelerateInterpolator());
                inSet.start();
            }
        });
        fadeOut.start();
    }

    // --- Sparkle icon pulse animation ---

    private void startSparkleAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivSparkle, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivSparkle, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(ivSparkle, "rotation", 0f, 15f, -15f, 0f);

        sparkleAnimator = new AnimatorSet();
        sparkleAnimator.playTogether(scaleX, scaleY, rotate);
        sparkleAnimator.setDuration(2000);
        sparkleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        sparkleAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isAdded()) {
                    ivSparkle.postDelayed(() -> {
                        if (isAdded() && sparkleAnimator != null) sparkleAnimator.start();
                    }, 1500);
                }
            }
        });
        sparkleAnimator.start();
    }

    // --- Shimmer loading animation ---

    private void showShimmer() {
        shimmerContainer.setVisibility(View.VISIBLE);
        rvResults.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        shimmerContainer.setVisibility(View.GONE);
    }

    private void startShimmerAnimation() {
        for (int i = 0; i < shimmerContainer.getChildCount(); i++) {
            View child = shimmerContainer.getChildAt(i);
            ValueAnimator anim = ValueAnimator.ofFloat(0.3f, 1f, 0.3f);
            anim.setDuration(1500);
            anim.setStartDelay(i * 200L);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addUpdateListener(a -> child.setAlpha((float) a.getAnimatedValue()));
            anim.start();
        }
    }

    // --- Search logic ---

    private void loadAllCourses() {
        if (allCoursesCall != null) allCoursesCall.cancel();
        allCoursesCall = courseApi.getCourses();
        allCoursesCall.enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(Call<CourseListResponse> call, Response<CourseListResponse> response) {
                if (!isAdded()) return;
                hideShimmer();
                displayedCourses.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getListCourse() != null) {
                    displayedCourses.addAll(response.body().getListCourse());
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.GONE);
                rvResults.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<CourseListResponse> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                hideShimmer();
            }
        });
    }

    private void searchCourses(String keyword) {
        cancelPending();

        pendingCall = courseApi.searchCourses(keyword);
        pendingCall.enqueue(new Callback<List<CourseSearchResult>>() {
            @Override
            public void onResponse(Call<List<CourseSearchResult>> call, Response<List<CourseSearchResult>> response) {
                if (!isAdded()) return;
                hideShimmer();
                displayedCourses.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (CourseSearchResult result : response.body()) {
                        Course c = new Course(result.getCourseId(), result.getTitle(), "", "Available");
                        c.setImageUrl(result.getImageUrl());
                        c.setPrice(result.getPrice());
                        displayedCourses.add(c);
                    }
                }
                adapter.notifyDataSetChanged();
                if (displayedCourses.isEmpty()) {
                    rvResults.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<CourseSearchResult>> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                hideShimmer();
                displayedCourses.clear();
                adapter.notifyDataSetChanged();
                rvResults.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Search failed. Check your connection.");
            }
        });
    }

    private void cancelPending() {
        if (pendingCall != null) {
            pendingCall.cancel();
            pendingCall = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debounceHandler.removeCallbacks(pendingSearch);
        stopHintRotation();
        cancelPending();
        if (allCoursesCall != null) allCoursesCall.cancel();
        if (sparkleAnimator != null) sparkleAnimator.cancel();
    }

    private void openCourseDetail(Course course) {
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseID());
        args.putString("courseTitle", course.getTitle());
        args.putString("courseDescription", course.getLearningMode());
        args.putString("imageUrl", course.getImageUrl());
        if (course.getPrice() != null) {
            args.putLong("coursePrice", course.getPrice());
        }
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.courseDetailFragment, args);
    }
}
