package com.eduflex.service;

import com.eduflex.dto.GamificationStatsDTO;

public interface GamificationService {

    /**
     * Get gamification stats for a user. Creates default stats if not exist.
     */
    GamificationStatsDTO getStatsByUserId(String userId);

    /**
     * Add XP to a user. Automatically recalculates level.
     * Level formula: level = xp / 100 + 1
     */
    GamificationStatsDTO addXp(String userId, int amount);

    /**
     * Update daily streak for a user.
     * - If lastStudyDate == yesterday → streak + 1
     * - If lastStudyDate == today → no change
     * - Otherwise → reset to 1
     */
    GamificationStatsDTO updateStreak(String userId);
}
