package com.eduflex.dto.gamification;

public class DailyQuestDTO {

    public record DailyQuestResponse(
        Long questId,
        String title,
        String description,
        String questType,
        int targetCount,
        int xpReward,
        int currentCount,
        boolean completed
    ) {}

    public record QuestProgressRequest(
        String questType,
        int increment
    ) {}
}
