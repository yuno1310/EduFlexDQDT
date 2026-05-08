package com.eduflex.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.model.DailyQuestResponse;

import java.util.ArrayList;
import java.util.List;

public class DailyQuestAdapter extends RecyclerView.Adapter<DailyQuestAdapter.ViewHolder> {

    private List<DailyQuestResponse> quests = new ArrayList<>();

    public void setQuests(List<DailyQuestResponse> quests) {
        this.quests = quests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_quest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyQuestResponse quest = quests.get(position);

        holder.tvTitle.setText(quest.getTitle());
        holder.tvDescription.setText(quest.getDescription());

        int current = quest.getCurrentCount();
        int target = quest.getTargetCount();
        int progressPercent = target > 0 ? (int) ((current / (float) target) * 100) : 0;
        holder.pbQuest.setProgress(Math.min(progressPercent, 100));

        String progressLabel = formatProgress(quest.getQuestType(), current, target);
        holder.tvProgress.setText(progressLabel);

        if (quest.isCompleted()) {
            holder.tvXp.setVisibility(View.GONE);
            holder.tvDone.setVisibility(View.VISIBLE);
        } else {
            holder.tvXp.setVisibility(View.VISIBLE);
            holder.tvXp.setText("+" + quest.getXpReward() + " XP");
            holder.tvDone.setVisibility(View.GONE);
        }
    }

    private String formatProgress(String questType, int current, int target) {
        if ("STUDY_TIME".equals(questType)) {
            int currentMin = current / 60;
            int targetMin = target / 60;
            return currentMin + " / " + targetMin + " min";
        }
        return current + " / " + target;
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvProgress, tvXp, tvDone;
        ProgressBar pbQuest;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_quest_title);
            tvDescription = view.findViewById(R.id.tv_quest_description);
            tvProgress = view.findViewById(R.id.tv_quest_progress);
            tvXp = view.findViewById(R.id.tv_quest_xp);
            tvDone = view.findViewById(R.id.tv_quest_done);
            pbQuest = view.findViewById(R.id.pb_quest);
        }
    }
}
