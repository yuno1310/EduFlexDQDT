package com.eduflex.android.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.eduflex.android.R;
import com.eduflex.android.model.AdminUserResponse.AdminUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<AdminUser> allUsers;
    private List<AdminUser> filteredUsers;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(AdminUser user, int position);
    }

    public AdminUserAdapter(List<AdminUser> users, OnDeleteClickListener deleteListener) {
        this.allUsers = users != null ? users : new ArrayList<>();
        this.filteredUsers = new ArrayList<>(this.allUsers);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = filteredUsers.get(position);

        String name = user.getFullName();
        // Avatar logic: Image or fallback letter
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            holder.ivUserAvatar.setVisibility(View.VISIBLE);
            holder.tvAvatarLetter.setVisibility(View.GONE);
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .transform(new CircleCrop())
                    .into(holder.ivUserAvatar);
        } else {
            holder.ivUserAvatar.setVisibility(View.GONE);
            holder.tvAvatarLetter.setVisibility(View.VISIBLE);
            String letter = (name != null && !name.isEmpty())
                    ? name.substring(0, 1).toUpperCase()
                    : "?";
            holder.tvAvatarLetter.setText(letter);

            // Color avatar based on position
            int[] colors = {0xFF00BCD4, 0xFF9C27B0, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63};
            int bgColor = colors[position % colors.length];
            holder.tvAvatarLetter.getBackground().setTint(bgColor);
        }

        // User name + admin tag
        String displayName = name != null ? name : "Unknown";
        if (user.isAdmin()) {
            String full = displayName + " (admin)";
            SpannableString ss = new SpannableString(full);
            int start = displayName.length() + 1; // after space
            ss.setSpan(new ForegroundColorSpan(Color.RED), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvName.setText(ss);
        } else {
            holder.tvName.setText(displayName);
        }

        holder.tvEmail.setText(user.getEmail());
        holder.tvXp.setText(user.getXp() + " XP");
        holder.tvLevel.setText("Lv." + user.getLevel());

        // Fire icon: tint with fire_active/fire_inactive — same as HomeFragment
        boolean studiedToday = isStudiedToday(user.getLastStudyDate());
        holder.tvStreak.setText(String.valueOf(user.getStreakDays()));
        int fireColorRes = studiedToday ? R.color.fire_active : R.color.fire_inactive;
        int fireColor = ContextCompat.getColor(holder.itemView.getContext(), fireColorRes);
        ImageViewCompat.setImageTintList(holder.ivFireIcon, ColorStateList.valueOf(fireColor));

        // Delete button — hide for admin users
        if (user.isAdmin()) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(user, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    /**
     * Filter the list by name or email.
     */
    public void filter(String query) {
        filteredUsers.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lower = query.toLowerCase().trim();
            for (AdminUser user : allUsers) {
                String name = user.getFullName() != null ? user.getFullName().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                if (name.contains(lower) || email.contains(lower)) {
                    filteredUsers.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Check if user studied today by comparing lastStudyDate with today's date.
     */
    private boolean isStudiedToday(String lastStudyDate) {
        if (lastStudyDate == null || lastStudyDate.isEmpty()) return false;
        try {
            String today = LocalDate.now().toString(); // "YYYY-MM-DD"
            return lastStudyDate.equals(today);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove user from list after successful deletion.
     */
    public void removeUser(int position) {
        if (position >= 0 && position < filteredUsers.size()) {
            AdminUser removed = filteredUsers.remove(position);
            allUsers.remove(removed);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvName, tvEmail, tvXp, tvLevel, tvStreak;
        ImageView ivUserAvatar, ivFireIcon;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tv_avatar_letter);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvXp = itemView.findViewById(R.id.tv_user_xp);
            tvLevel = itemView.findViewById(R.id.tv_user_level);
            tvStreak = itemView.findViewById(R.id.tv_user_streak);
            ivFireIcon = itemView.findViewById(R.id.iv_fire_icon);
            btnDelete = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}
