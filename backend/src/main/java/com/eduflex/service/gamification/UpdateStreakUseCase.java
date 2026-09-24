package com.eduflex.service.gamification;

import com.eduflex.dto.gamification.UpdateStreakDTO;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UpdateStreakUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Autowired
    private CheckAndAwardBadgesUseCase checkAndAwardBadgesUseCase;

    @Autowired
    private Clock clock;

    @Transactional
    public UpdateStreakDTO.UpdateStreakResponse execute(UUID userId) {
        var locked = gamificationStatsRepository.ensureAndLock(userId);
        var stats = getGamificationStatsUseCase.mapToResponse(locked);
        LocalDate today = LocalDate.now(clock);
        LocalDate lastStudy = locked.record.getLastStudyDate();

        int newStreak;
        if (lastStudy == null) {
            newStreak = 1;
        } else if (lastStudy.equals(today)) {
            return new UpdateStreakDTO.UpdateStreakResponse(
                    stats.id(), stats.userId(), stats.xp(),
                    stats.level(), stats.streakDays(), stats.lastStudyDate()
            );
        } else if (lastStudy.equals(today.minusDays(1))) {
            newStreak = stats.streakDays() + 1;
        } else {
            newStreak = 1;
        }

        gamificationStatsRepository.updateStreak(userId, newStreak, today);

        // Check streak badges (7-day, 30-day)
        checkAndAwardBadgesUseCase.checkStreakBadges(userId, newStreak);

        return new UpdateStreakDTO.UpdateStreakResponse(
                stats.id(), stats.userId(), stats.xp(), stats.level(), newStreak, today
        );
    }
}
