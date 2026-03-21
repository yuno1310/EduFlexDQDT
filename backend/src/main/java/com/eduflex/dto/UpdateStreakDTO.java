package com.eduflex.dto;

import java.time.LocalDate;
import java.util.UUID;

public class UpdateStreakDTO {
    public record UpdateStreakResponse(
        Long id,
        UUID userId,
        int xp,
        int level,
        int streakDays,
        LocalDate lastStudyDate
    ) {}
}
