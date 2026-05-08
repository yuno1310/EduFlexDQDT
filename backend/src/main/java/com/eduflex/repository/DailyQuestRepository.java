package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Repository
public class DailyQuestRepository {

    @Autowired
    private DSLContext dsl;

    public List<QuestRow> findAllQuests() {
        return dsl.fetch("SELECT id, title, description, quest_type, target_count, xp_reward FROM daily_quests ORDER BY id")
            .map(r -> new QuestRow(
                r.get("id", Long.class),
                r.get("title", String.class),
                r.get("description", String.class),
                r.get("quest_type", String.class),
                r.get("target_count", Integer.class),
                r.get("xp_reward", Integer.class)
            ));
    }

    public ProgressRow findProgress(UUID userId, Long questId, LocalDate date) {
        var record = dsl.fetchOne(
            "SELECT current_count, completed FROM user_daily_quest_progress WHERE user_id = ? AND quest_id = ? AND quest_date = ?",
            userId, questId, date
        );
        if (record == null) return null;
        return new ProgressRow(
            record.get("current_count", Integer.class),
            record.get("completed", Boolean.class)
        );
    }

    public void upsertProgress(UUID userId, Long questId, LocalDate date, int delta, boolean reset) {
        if (reset) {
            dsl.execute(
                "INSERT INTO user_daily_quest_progress (user_id, quest_id, quest_date, current_count, completed) " +
                "VALUES (?, ?, ?, 0, false) " +
                "ON CONFLICT (user_id, quest_id, quest_date) DO UPDATE SET current_count = 0, completed = false",
                userId, questId, date
            );
        } else {
            dsl.execute(
                "INSERT INTO user_daily_quest_progress (user_id, quest_id, quest_date, current_count, completed) " +
                "VALUES (?, ?, ?, ?, false) " +
                "ON CONFLICT (user_id, quest_id, quest_date) DO UPDATE " +
                "SET current_count = user_daily_quest_progress.current_count + EXCLUDED.current_count",
                userId, questId, date, delta
            );
        }
    }

    public void markCompleted(UUID userId, Long questId, LocalDate date) {
        dsl.execute(
            "UPDATE user_daily_quest_progress SET completed = true WHERE user_id = ? AND quest_id = ? AND quest_date = ?",
            userId, questId, date
        );
    }

    public Long findQuestIdByType(String questType) {
        var record = dsl.fetchOne("SELECT id FROM daily_quests WHERE quest_type = ?", questType);
        return record != null ? record.get("id", Long.class) : null;
    }

    public record QuestRow(Long id, String title, String description, String questType, int targetCount, int xpReward) {}
    public record ProgressRow(int currentCount, boolean completed) {}
}
