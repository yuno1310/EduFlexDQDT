package com.eduflex.dto;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;

public class AddXpDTO {
    public record AddXpRequest(@Positive int amount) {}

    public record AddXpResponse(
        Long id,
        UUID userId,
        int xp,
        int level,
        int streakDays,
        LocalDate lastStudyDate
    ) {}
}
