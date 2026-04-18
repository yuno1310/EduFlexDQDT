package com.eduflex.android.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.BadgeResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<BadgeResponse> allBadges;
    private final Set<Long> earnedBadgeIds;

    public BadgeAdapter(List<BadgeResponse> allBadges, Set<Long> earnedBadgeIds) {
        this.allBadges = allBadges;
        this.earnedBadgeIds = earnedBadgeIds != null ? earnedBadgeIds : new HashSet<>();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeResponse badge = allBadges.get(position);
        boolean earned = earnedBadgeIds.contains(badge.getId());

        holder.tvBadgeName.setText(badge.getName());

        // Pick icon based on condition type
        int iconRes = getIconForCondition(badge.getConditionType());
        holder.ivBadgeIcon.setImageResource(iconRes);

        if (earned) {
            // Earned — colored icon, cyan border
            holder.ivBadgeIcon.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_earned));
            int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.cyan_primary_dark);
            ImageViewCompat.setImageTintList(holder.ivBadgeIcon, ColorStateList.valueOf(color));
            holder.ivBadgeLock.setVisibility(View.GONE);
            holder.ivBadgeIcon.setAlpha(1.0f);
            holder.tvBadgeName.setAlpha(1.0f);
        } else {
            // Locked — gray icon, muted
            holder.ivBadgeIcon.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_badge_locked));
            int color = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted);
            ImageViewCompat.setImageTintList(holder.ivBadgeIcon, ColorStateList.valueOf(color));
            holder.ivBadgeLock.setVisibility(View.VISIBLE);
            holder.ivBadgeIcon.setAlpha(0.5f);
            holder.tvBadgeName.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return allBadges.size();
    }

    /**
     * Maps condition_type to a system icon. In production, use custom drawables.
     */
    private int getIconForCondition(String conditionType) {
        if (conditionType == null) return android.R.drawable.btn_star_big_on;
        if (conditionType.startsWith("COURSE_")) {
            return android.R.drawable.ic_menu_agenda; // Document/Certificate like icon
        }
        
        switch (conditionType) {
            case "FIRST_LOGIN":
                return android.R.drawable.ic_menu_myplaces;
            case "STREAK_7":
                return android.R.drawable.ic_menu_recent_history;
            case "STREAK_30":
                return android.R.drawable.ic_menu_today;
            case "XP_500":
                return android.R.drawable.btn_star_big_on;
            case "XP_1000":
                return android.R.drawable.ic_menu_compass;
            default:
                return android.R.drawable.btn_star_big_on;
        }
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBadgeIcon;
        ImageView ivBadgeLock;
        TextView tvBadgeName;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBadgeIcon = itemView.findViewById(R.id.iv_badge_icon);
            ivBadgeLock = itemView.findViewById(R.id.iv_badge_lock);
            tvBadgeName = itemView.findViewById(R.id.tv_badge_name);
        }
    }
}
