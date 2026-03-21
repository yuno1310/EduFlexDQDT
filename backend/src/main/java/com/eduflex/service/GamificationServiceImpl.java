package com.eduflex.service;

import com.eduflex.dto.GamificationStatsDTO;
import com.eduflex.exception.ResourceNotFoundException;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.jooq.impl.DSL.*;

@Service
public class GamificationServiceImpl implements GamificationService {

    private final DSLContext dsl;

    public GamificationServiceImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public GamificationStatsDTO getStatsByUserId(String userId) {
        var record = dsl.selectFrom(table("gamification_stats"))
                .where(field("user_id").eq(userId))
                .fetchOne();

        if (record == null) {
            // Auto-create default stats for this user
            return createDefaultStats(userId);
        }

        return mapToDTO(record);
    }

    @Override
    @Transactional
    public GamificationStatsDTO addXp(String userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("XP amount must be positive");
        }

        // Ensure stats exist
        getStatsByUserId(userId);

        // Update XP and recalculate level
        dsl.update(table("gamification_stats"))
                .set(field("xp"), field("xp", Integer.class).plus(amount))
                .set(field("level"), field("xp", Integer.class).plus(amount).div(100).plus(1))
                .where(field("user_id").eq(userId))
                .execute();

        return getStatsByUserId(userId);
    }

    @Override
    @Transactional
    public GamificationStatsDTO updateStreak(String userId) {
        // Ensure stats exist
        GamificationStatsDTO stats = getStatsByUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDate lastStudy = stats.getLastStudyDate();

        int newStreak;
        if (lastStudy == null) {
            newStreak = 1;
        } else if (lastStudy.equals(today)) {
            // Already studied today — no change
            return stats;
        } else if (lastStudy.equals(today.minusDays(1))) {
            // Consecutive day — increment streak
            newStreak = stats.getStreakDays() + 1;
        } else {
            // Streak broken — reset to 1
            newStreak = 1;
        }

        dsl.update(table("gamification_stats"))
                .set(field("streak_days"), newStreak)
                .set(field("last_study_date"), today)
                .where(field("user_id").eq(userId))
                .execute();

        stats.setStreakDays(newStreak);
        stats.setLastStudyDate(today);
        return stats;
    }

    // ── Private helpers ──

    private GamificationStatsDTO createDefaultStats(String userId) {
        // Verify user exists
        var userExists = dsl.selectCount()
                .from(table("users"))
                .where(field("user_id").eq(userId))
                .fetchOne(0, int.class);

        if (userExists == null || userExists == 0) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        dsl.insertInto(table("gamification_stats"))
                .set(field("user_id"), userId)
                .set(field("xp"), 0)
                .set(field("level"), 1)
                .set(field("streak_days"), 0)
                .execute();

        return GamificationStatsDTO.builder()
                .userId(userId)
                .xp(0)
                .level(1)
                .streakDays(0)
                .build();
    }

    private GamificationStatsDTO mapToDTO(org.jooq.Record record) {
        return GamificationStatsDTO.builder()
                .id(record.get(field("id", Long.class)))
                .userId(record.get(field("user_id", String.class)))
                .xp(record.get(field("xp", Integer.class)))
                .level(record.get(field("level", Integer.class)))
                .streakDays(record.get(field("streak_days", Integer.class)))
                .lastStudyDate(record.get(field("last_study_date", LocalDate.class)))
                .build();
    }
}
