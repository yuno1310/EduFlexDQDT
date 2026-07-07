package com.eduflex.dto.gamification;

import java.time.LocalDateTime;
import java.util.UUID;

public class GetUserBadgesDTO {
    public record GetUserBadgesResponse(
        Long id,
        UUID userId,
        Long badgeId,
        String badgeName,
        String badgeDescription,
        String badgeIconUrl,
        LocalDateTime earnedAt
    ) {}
}
