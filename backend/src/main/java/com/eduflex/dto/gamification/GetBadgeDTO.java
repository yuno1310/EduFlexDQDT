package com.eduflex.dto.gamification;

import java.time.LocalDateTime;

public class GetBadgeDTO {
    public record CourseBadgeResponse(
        boolean success,
        String message,
        String badgeName,
        String description,
        String iconUrl,
        LocalDateTime earnedAt
    ) {}
}
