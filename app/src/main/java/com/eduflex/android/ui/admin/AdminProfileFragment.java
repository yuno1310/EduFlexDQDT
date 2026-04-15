package com.eduflex.android.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.eduflex.android.AdminActivity;
import com.eduflex.android.R;
import com.eduflex.android.auth.TokenManager;

/**
 * Simplified admin profile — shows admin info and logout only.
 */
public class AdminProfileFragment extends Fragment {

    public AdminProfileFragment() {
        super(R.layout.fragment_admin_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TokenManager tokenManager = new TokenManager(requireContext());

        TextView tvAvatar = view.findViewById(R.id.tv_admin_avatar);
        TextView tvName = view.findViewById(R.id.tv_admin_name);
        TextView tvEmail = view.findViewById(R.id.tv_admin_email);

        // Display real name from login
        String fullName = tokenManager.getFullName();
        String email = tokenManager.getEmail();

        String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : "Administrator";
        String letter = displayName.substring(0, 1).toUpperCase();

        tvAvatar.setText(letter);
        tvName.setText(displayName);
        tvEmail.setText(email != null ? email : "admin@eduflex.com");

        // Logout
        view.findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        if (getActivity() instanceof AdminActivity) {
                            ((AdminActivity) getActivity()).logoutAdmin();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
