package com.eduflex.entity;

import com.eduflex.generated.tables.GamificationStats;
import com.eduflex.generated.tables.records.GamificationStatsRecord;
import java.util.UUID;

public class GamificationStatsDbO {
    public GamificationStatsRecord record;

    public GamificationStatsDbO(UUID userId, int xp, int level, int streakDays) {
        record = GamificationStats.GAMIFICATION_STATS.newRecord();
        record.setUserId(userId);
        record.setXp(xp);
        record.setLevel(level);
        record.setStreakDays(streakDays);
    }

    public GamificationStatsDbO(GamificationStatsRecord record) {
        this.record = record;
    }
}
