package com.eduflex.service.gamification;

import com.eduflex.dto.gamification.GetGamificationStatsDTO;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import com.eduflex.monitoring.EduFlexMetrics;

/**
 * Awards +10 XP once per calendar day on check-in.
 * Safe to call multiple times — server prevents duplicates via last_login_xp_date.
 */
@Service
public class DailyCheckinUseCase {

    private static final int DAILY_LOGIN_XP = 10;

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private EduFlexMetrics metrics;

    @Transactional
    public GetGamificationStatsDTO.GetGamificationStatsResponse execute(UUID userId) {
        gamificationStatsRepository.ensureAndLock(userId);
        LocalDate today = LocalDate.now(clock);
        boolean awarded = gamificationStatsRepository.awardDailyLoginXp(userId, today, DAILY_LOGIN_XP);
        if (awarded) metrics.reward("checkin");

        return getGamificationStatsUseCase.execute(userId);
    }
}
