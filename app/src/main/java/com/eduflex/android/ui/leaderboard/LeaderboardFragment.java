package com.eduflex.android.ui.leaderboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.adapter.LeaderboardAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.LeaderboardResponse;
import com.eduflex.android.model.LeaderboardResponse.LeaderboardItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";

    private RecyclerView rvLeaderboard;
    private ProgressBar progressLoading;
    private View llEmptyState;
    private TextView tvMyRank, tvMyName, tvMyXp, tvTotalPlayers;

    private GamificationApi api;
    private String userId;

    public LeaderboardFragment() {
        super(R.layout.fragment_leaderboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        api = ApiClient.createAuthenticatedService(GamificationApi.class);
        TokenManager tokenManager = new TokenManager(requireContext());
        userId = tokenManager.getUserId();

        bindViews(view);
        setupBackButton(view);
        fetchLeaderboard();
    }

    private void bindViews(View view) {
        rvLeaderboard = view.findViewById(R.id.rv_leaderboard);
        progressLoading = view.findViewById(R.id.progress_loading);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        tvMyRank = view.findViewById(R.id.tv_my_rank);
        tvMyName = view.findViewById(R.id.tv_my_name);
        tvMyXp = view.findViewById(R.id.tv_my_xp);
        tvTotalPlayers = view.findViewById(R.id.tv_total_players);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
    }

    private void fetchLeaderboard() {
        if (userId == null) return;

        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        api.getLeaderboard(userId, 50).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardResponse> call,
                                   @NonNull Response<LeaderboardResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<LeaderboardItem> items = response.body().getLeaderBoard();
                    if (items != null && !items.isEmpty()) {
                        showLeaderboard(items);
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Leaderboard load failed: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void showLeaderboard(List<LeaderboardItem> items) {
        rvLeaderboard.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        rvLeaderboard.setAdapter(new LeaderboardAdapter(items, userId));
        tvTotalPlayers.setText("Players: " + items.size());

        // Find current user in the list
        for (LeaderboardItem item : items) {
            if (item.getUserId() != null && item.getUserId().equals(userId)) {
                tvMyRank.setText("#" + item.getRank());
                String name = item.getFullName();
                tvMyName.setText(name != null && !name.isEmpty() ? name : "You");
                tvMyXp.setText(String.valueOf(item.getXp()));

                // Set rank badge color
                switch (item.getRank()) {
                    case 1: tvMyRank.setBackgroundResource(R.drawable.bg_rank_gold); break;
                    case 2: tvMyRank.setBackgroundResource(R.drawable.bg_rank_silver); break;
                    case 3: tvMyRank.setBackgroundResource(R.drawable.bg_rank_bronze); break;
                    default: tvMyRank.setBackgroundResource(R.drawable.bg_rank_circle); break;
                }
                return;
            }
        }

        // User not in top 50
        tvMyRank.setText("#-");
        tvMyName.setText("Not ranked yet");
        tvMyXp.setText("0");
    }

    private void showEmptyState() {
        rvLeaderboard.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
    }
}
