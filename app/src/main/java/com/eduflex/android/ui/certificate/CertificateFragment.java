package com.eduflex.android.ui.certificate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CertificateFragment extends Fragment {

    private static final String PREF_PROFILE = "eduflex_profile";
    private static final String PREF_COURSE_PROGRESS = "course_progress";
    private static final String PREF_CERTIFICATE = "course_certificate";
    private static final String KEY_NAME = "user_name";

    public CertificateFragment() {
        super(R.layout.fragment_certificate);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String courseId = args != null ? args.getString("courseId", "") : "";
        String courseTitle = args != null ? args.getString("courseTitle", "Course") : "Course";

        TextView tvStatus = view.findViewById(R.id.tv_certificate_status);
        TextView tvLearnerName = view.findViewById(R.id.tv_certificate_learner_name);
        TextView tvCourseName = view.findViewById(R.id.tv_certificate_course_name);
        TextView tvDate = view.findViewById(R.id.tv_certificate_date);
        Button btnBack = view.findViewById(R.id.btn_back_certificate);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        SharedPreferences profilePrefs = requireContext().getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);
        String learnerName = profilePrefs.getString(KEY_NAME, "EduFlex Learner");

        SharedPreferences progressPrefs = requireContext().getSharedPreferences(PREF_COURSE_PROGRESS, Context.MODE_PRIVATE);
        int progress = courseId.isEmpty() ? 0 : progressPrefs.getInt(courseId, 0);

        tvLearnerName.setText(learnerName);
        tvCourseName.setText(courseTitle);

        if (progress < 100) {
            tvStatus.setText("Certificate is locked. Reach 100% progress to claim it.");
            tvDate.setText("--/--/----");
            return;
        }

        tvStatus.setText("Congratulations! You completed this course.");

        SharedPreferences certPrefs = requireContext().getSharedPreferences(PREF_CERTIFICATE, Context.MODE_PRIVATE);
        String date = certPrefs.getString(courseId + "_issued_at", null);
        if (date == null) {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            certPrefs.edit().putString(courseId + "_issued_at", date).apply();
        }
        tvDate.setText(date);
    }
}
