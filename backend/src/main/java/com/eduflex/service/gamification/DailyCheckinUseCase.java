package com.eduflex.service.gamification;

import com.eduflex.dto.gamification.GetGamificationStatsDTO;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

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

    @Transactional
    public GetGamificationStatsDTO.GetGamificationStatsResponse execute(UUID userId) {
        // Ensure stats row exists
        getGamificationStatsUseCase.execute(userId);

        // Award XP only if not yet awarded today
        LocalDate today = LocalDate.now();
        LocalDate lastDate = gamificationStatsRepository.getLastLoginXpDate(userId);

        if (lastDate == null || !lastDate.equals(today)) {
            gamificationStatsRepository.updateXpAndLevel(userId, DAILY_LOGIN_XP);
            gamificationStatsRepository.setLastLoginXpDate(userId, today);
        }

        return getGamificationStatsUseCase.execute(userId);
    }
}
