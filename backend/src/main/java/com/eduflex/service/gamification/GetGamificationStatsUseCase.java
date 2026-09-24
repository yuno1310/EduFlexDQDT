package com.eduflex.service.gamification;

import com.eduflex.dto.gamification.GetGamificationStatsDTO;
import com.eduflex.entity.gamification.GamificationStatsDbO;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import com.eduflex.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class GetGamificationStatsUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    @Transactional
    public GetGamificationStatsDTO.GetGamificationStatsResponse execute(UUID userId) {
        var stats = gamificationStatsRepository.findByUserId(userId);

        if (stats != null) {
            return mapToResponse(stats);
        }

        if (userRepository.find_by_id(userId) == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        gamificationStatsRepository.ensureAndLock(userId);
        return mapToResponse(gamificationStatsRepository.findByUserId(userId));
    }

    public GetGamificationStatsDTO.GetGamificationStatsResponse mapToResponse(GamificationStatsDbO stats) {
        int displayStreak = stats.record.getStreakDays() != null ? stats.record.getStreakDays() : 0;
        LocalDate lastStudyDate = stats.record.getLastStudyDate();
        LocalDate today = LocalDate.now(clock);

        if (lastStudyDate != null && lastStudyDate.isBefore(today.minusDays(1))) {
            displayStreak = 0;
        }

        return new GetGamificationStatsDTO.GetGamificationStatsResponse(
                stats.record.getId(),
                stats.record.getUserId(),
                stats.record.getXp(),
                stats.record.getLevel(),
                displayStreak,
                lastStudyDate
        );
    }
}
