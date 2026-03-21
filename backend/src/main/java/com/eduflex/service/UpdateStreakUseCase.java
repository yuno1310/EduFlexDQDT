package com.eduflex.service;

import com.eduflex.dto.UpdateStreakDTO;
import com.eduflex.repository.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UpdateStreakUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Transactional
    public UpdateStreakDTO.UpdateStreakResponse execute(UUID userId) {
        var stats = getGamificationStatsUseCase.execute(userId);
        LocalDate today = LocalDate.now();
        LocalDate lastStudy = stats.lastStudyDate();

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

        return new UpdateStreakDTO.UpdateStreakResponse(
                stats.id(), stats.userId(), stats.xp(), stats.level(), newStreak, today
        );
    }
}
