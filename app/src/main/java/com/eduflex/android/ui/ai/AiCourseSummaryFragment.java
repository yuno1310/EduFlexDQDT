package com.eduflex.android.ui.ai;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.eduflex.android.BuildConfig;
import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.LessonApi;
import com.eduflex.android.model.Lesson;
import com.eduflex.android.model.LessonListResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiCourseSummaryFragment extends Fragment {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String courseId;
    private String courseTitle;

    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private ScrollView scrollSummary;
    private TextView tvSummary;
    private TextView tvError;
    private TextView tvCourseTitle;

    private final OkHttpClient httpClient = new OkHttpClient();
    private LessonApi lessonApi;

    public AiCourseSummaryFragment() {
        super(R.layout.fragment_ai_course_summary);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lessonApi = ApiClient.createAuthenticatedService(LessonApi.class);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId", "");
            courseTitle = getArguments().getString("courseTitle", "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutError = view.findViewById(R.id.layout_error);
        scrollSummary = view.findViewById(R.id.scroll_summary);
        tvSummary = view.findViewById(R.id.tv_summary);
        tvError = view.findViewById(R.id.tv_error);
        tvCourseTitle = view.findViewById(R.id.tv_course_title);

        tvCourseTitle.setText(courseTitle);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
        view.findViewById(R.id.btn_retry).setOnClickListener(v -> fetchSummary());

        fetchSummary();
    }

    private void fetchSummary() {
        showLoading();
        if (courseId == null || courseId.isEmpty()) {
            tryGemini(buildPromptNoLessons());
            return;
        }
        lessonApi.getLessons(courseId).enqueue(new retrofit2.Callback<LessonListResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<LessonListResponse> call,
                                   @NonNull retrofit2.Response<LessonListResponse> response) {
                String prompt;
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<Lesson> lessons = response.body().getListLesson();
                    prompt = (lessons != null && !lessons.isEmpty())
                            ? buildPromptWithLessons(lessons)
                            : buildPromptNoLessons();
                } else {
                    prompt = buildPromptNoLessons();
                }
                tryGemini(prompt);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<LessonListResponse> call, @NonNull Throwable t) {
                tryGemini(buildPromptNoLessons());
            }
        });
    }

    private String buildPromptWithLessons(List<Lesson> lessons) {
        StringBuilder lessonList = new StringBuilder();
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            String type = lesson.getContentType() != null ? lesson.getContentType() : "reading";
            lessonList.append(i + 1).append(". ")
                    .append(lesson.getTitle())
                    .append(" [").append(type).append("]")
                    .append("\n");
        }
        return "You are an educational assistant. Given the course title and its list of lessons below, "
                + "write a concise, engaging summary (3-5 paragraphs) that explains:\n"
                + "1. What this course covers overall\n"
                + "2. What key skills or knowledge the learner will gain from these lessons\n"
                + "3. Who this course is best suited for\n"
                + "4. What learning formats are used (e.g. video, reading, quizzes)\n\n"
                + "Course title: " + courseTitle + "\n"
                + "Lessons (with format type):\n" + lessonList
                + "\nWrite in a friendly, motivating tone suitable for a mobile learning app.";
    }

    private String buildPromptNoLessons() {
        return "You are an educational assistant. Given the course title below, "
                + "write a concise, engaging summary (3-5 paragraphs) that explains:\n"
                + "1. What this course is likely about\n"
                + "2. What key skills or knowledge the learner might gain\n"
                + "3. Who this course is best suited for\n\n"
                + "Course title: " + courseTitle + "\n\n"
                + "Write in a friendly, motivating tone suitable for a mobile learning app.";
    }

    // --- Primary: Gemini 2.0 Flash ---

    private void tryGemini(String prompt) {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey.isEmpty() || apiKey.equals("your_gemini_api_key_here")) {
            tryGroq(prompt, "Gemini API key not configured.");
            return;
        }

        try {
            JSONObject textPart = new JSONObject().put("text", prompt);
            JSONObject content = new JSONObject().put("parts", new JSONArray().put(textPart));
            JSONObject body = new JSONObject().put("contents", new JSONArray().put(content));

            Request request = new Request.Builder()
                    .url(GEMINI_URL + "?key=" + apiKey)
                    .addHeader("content-type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    tryGroq(prompt, "Gemini network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        tryGroq(prompt, "Gemini error (" + response.code() + ").");
                        return;
                    }
                    try {
                        String text = new JSONObject(responseBody)
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                        showResult(text);
                    } catch (Exception e) {
                        tryGroq(prompt, "Gemini parse error.");
                    }
                }
            });
        } catch (Exception e) {
            tryGroq(prompt, "Gemini request build failed.");
        }
    }

    // --- Fallback: Groq ---

    private void tryGroq(String prompt, String primaryFailReason) {
        String apiKey = BuildConfig.GROQ_API_KEY;
        if (apiKey.isEmpty() || apiKey.equals("your_groq_api_key_here")) {
            showError(primaryFailReason + "\nGroq API key not configured either.");
            return;
        }

        try {
            JSONObject message = new JSONObject()
                    .put("role", "user")
                    .put("content", prompt);

            JSONObject body = new JSONObject()
                    .put("model", GROQ_MODEL)
                    .put("messages", new JSONArray().put(message));

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("content-type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    showError("Both providers failed.\nGroq: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        showError("Both providers failed.\nGroq error (" + response.code() + ").");
                        return;
                    }
                    try {
                        String text = new JSONObject(responseBody)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        showResult(text);
                    } catch (Exception e) {
                        showError("Both providers failed. Could not parse response.");
                    }
                }
            });
        } catch (Exception e) {
            showError("Both providers failed.");
        }
    }

    // --- UI helpers ---

    private void showLoading() {
        requireActivity().runOnUiThread(() -> {
            layoutLoading.setVisibility(View.VISIBLE);
            layoutError.setVisibility(View.GONE);
            scrollSummary.setVisibility(View.GONE);
        });
    }

    private void showResult(String text) {
        requireActivity().runOnUiThread(() -> {
            tvSummary.setText(text);
            scrollSummary.setVisibility(View.VISIBLE);
            layoutLoading.setVisibility(View.GONE);
            layoutError.setVisibility(View.GONE);
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() -> {
            tvError.setText(message);
            layoutError.setVisibility(View.VISIBLE);
            layoutLoading.setVisibility(View.GONE);
            scrollSummary.setVisibility(View.GONE);
        });
    }
}
