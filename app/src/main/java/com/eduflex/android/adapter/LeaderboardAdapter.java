package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.LeaderboardResponse.LeaderboardItem;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardItem> items;
    private final String currentUserId;

    public LeaderboardAdapter(List<LeaderboardItem> items, String currentUserId) {
        this.items = items;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardItem item = items.get(position);
        int rank = item.getRank();

        // Rank badge with medal colors for top 3
        holder.tvRank.setText(String.valueOf(rank));
        switch (rank) {
            case 1:
                holder.tvRank.setBackgroundResource(R.drawable.bg_rank_gold);
                break;
            case 2:
                holder.tvRank.setBackgroundResource(R.drawable.bg_rank_silver);
                break;
            case 3:
                holder.tvRank.setBackgroundResource(R.drawable.bg_rank_bronze);
                break;
            default:
                holder.tvRank.setBackgroundResource(R.drawable.bg_rank_circle);
                break;
        }

        // Name
        String name = item.getFullName();
        if (name == null || name.isEmpty()) {
            name = "Learner";
        }
        holder.tvName.setText(name);

        // Level & XP
        holder.tvLevel.setText("Level " + item.getLevel());
        holder.tvXp.setText(String.valueOf(item.getXp()));

        // Highlight current user row
        if (item.getUserId() != null && item.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundColor(0x1A00BCD4); // 10% cyan tint
            holder.tvName.setText(name + " (You)");
        } else {
            holder.itemView.setBackgroundColor(0x00000000); // transparent
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvLevel, tvXp;
        ImageView ivAvatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvXp = itemView.findViewById(R.id.tv_xp);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
