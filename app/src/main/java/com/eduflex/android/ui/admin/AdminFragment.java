package com.eduflex.android.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.adapter.AdminUserAdapter;
import com.eduflex.android.api.AdminApi;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.model.AdminUserResponse;
import com.eduflex.android.model.AdminUserResponse.AdminUser;
import com.eduflex.android.model.DeleteUserResponse;

import java.time.LocalDate;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminFragment extends Fragment {

    private static final String TAG = "AdminFragment";

    private RecyclerView rvUsers;
    private ProgressBar progressLoading;
    private View llEmptyState;
    private TextView tvTotalUsers, tvActiveLearners, tvTotalXp;
    private EditText etSearch;

    private AdminApi adminApi;
    private AdminUserAdapter adapter;

    public AdminFragment() {
        super(R.layout.fragment_admin);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminApi = ApiClient.createAuthenticatedService(AdminApi.class);
        bindViews(view);
        setupSearch();
        fetchUsers();
    }

    private void bindViews(View view) {
        rvUsers = view.findViewById(R.id.rv_users);
        progressLoading = view.findViewById(R.id.progress_loading);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvActiveLearners = view.findViewById(R.id.tv_active_learners);
        tvTotalXp = view.findViewById(R.id.tv_total_xp);
        etSearch = view.findViewById(R.id.et_search);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        progressLoading.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        adminApi.getAllUsers().enqueue(new Callback<AdminUserResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminUserResponse> call,
                                   @NonNull Response<AdminUserResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<AdminUser> users = response.body().getUsers();
                    if (users != null && !users.isEmpty()) {
                        showUsers(users);
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load users: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminUserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void showUsers(List<AdminUser> users) {
        rvUsers.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        // Update summary stats
        int totalXp = 0;
        int activeLearners = 0;
        String today = LocalDate.now().toString(); // "YYYY-MM-DD"
        for (AdminUser user : users) {
            totalXp += user.getXp();
            // Active = studied today (same logic as fire icon)
            if (today.equals(user.getLastStudyDate())) {
                activeLearners++;
            }
        }
        tvTotalUsers.setText(String.valueOf(users.size()));
        tvActiveLearners.setText(String.valueOf(activeLearners));
        tvTotalXp.setText(String.valueOf(totalXp));

        adapter = new AdminUserAdapter(users, this::confirmDeleteUser);
        rvUsers.setAdapter(adapter);
    }

    private void confirmDeleteUser(AdminUser user, int position) {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete \"" + user.getFullName() + "\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(AdminUser user, int position) {
        adminApi.deleteUser(user.getUserId()).enqueue(new Callback<DeleteUserResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteUserResponse> call,
                                   @NonNull Response<DeleteUserResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.removeUser(position);

                    // Update total count
                    int current = Integer.parseInt(tvTotalUsers.getText().toString());
                    tvTotalUsers.setText(String.valueOf(Math.max(0, current - 1)));

                    Log.d(TAG, "User deleted: " + user.getEmail());
                } else {
                    Log.e(TAG, "Delete failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteUserResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Delete network error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        rvUsers.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }
}
