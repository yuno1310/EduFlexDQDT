package com.eduflex.dto;

import java.time.LocalDate;
import java.util.UUID;

public class GetGamificationStatsDTO {
    public record GetGamificationStatsResponse(
        Long id,
        UUID userId,
        int xp,
        int level,
        int streakDays,
        LocalDate lastStudyDate
    ) {}
}
