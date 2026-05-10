package com.eduflex.service;

import com.eduflex.dto.GetGamificationStatsDTO;
import com.eduflex.entity.GamificationStatsDbO;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.GamificationStatsRepository;
import com.eduflex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class GetGamificationStatsUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private UserRepository userRepository;

    public GetGamificationStatsDTO.GetGamificationStatsResponse execute(UUID userId) {
        var stats = gamificationStatsRepository.findByUserId(userId);

        if (stats != null) {
            return mapToResponse(stats);
        }

        if (userRepository.find_by_id(userId) == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        var defaultStats = new GamificationStatsDbO(userId, 0, 1, 0);
        gamificationStatsRepository.save(defaultStats);

       var created = gamificationStatsRepository.findByUserId(userId);
       return mapToResponse(created);
    }

    public GetGamificationStatsDTO.GetGamificationStatsResponse mapToResponse(GamificationStatsDbO stats) {
        int displayStreak = stats.record.getStreakDays() != null ? stats.record.getStreakDays() : 0;
        LocalDate lastStudyDate = stats.record.getLastStudyDate();
        LocalDate today = LocalDate.now();

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
