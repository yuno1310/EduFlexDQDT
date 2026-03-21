package com.eduflex.service;

import com.eduflex.dto.AddXpDTO;
import com.eduflex.repository.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddXpUseCase {

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Transactional
    public AddXpDTO.AddXpResponse execute(UUID userId, AddXpDTO.AddXpRequest request) {
        if (request.amount() <= 0) {
            throw new IllegalArgumentException("XP amount must be positive");
        }

        getGamificationStatsUseCase.execute(userId); // ensure stats exist

        gamificationStatsRepository.updateXpAndLevel(userId, request.amount());

        var updated = gamificationStatsRepository.findByUserId(userId);
        return new AddXpDTO.AddXpResponse(
                updated.record.getId(),
                updated.record.getUserId(),
                updated.record.getXp(),
                updated.record.getLevel(),
                updated.record.getStreakDays(),
                updated.record.getLastStudyDate()
        );
    }
}
