package com.eduflex.service;

import com.eduflex.dto.BadgeDTO;
import com.eduflex.dto.UserBadgeDTO;

import java.util.List;

public interface BadgeService {

    /**
     * Get all available badges.
     */
    List<BadgeDTO> getAllBadges();

    /**
     * Get all badges earned by a specific user.
     */
    List<UserBadgeDTO> getUserBadges(String userId);

    /**
     * Award a badge to a user. Throws if already earned.
     */
    UserBadgeDTO awardBadge(String userId, Long badgeId);
}
