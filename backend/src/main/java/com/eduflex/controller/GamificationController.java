package com.eduflex.controller;

import com.eduflex.dto.AddXpDTO;
import com.eduflex.dto.GetGamificationStatsDTO;
import com.eduflex.dto.UpdateStreakDTO;
import com.eduflex.service.AddXpUseCase;
import com.eduflex.service.GetGamificationStatsUseCase;
import com.eduflex.service.UpdateStreakUseCase;
import com.eduflex.repository.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}")
public class GamificationController {

    private static final int DAILY_LOGIN_XP = 10;

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Autowired
    private AddXpUseCase addXpUseCase;

    @Autowired
    private UpdateStreakUseCase updateStreakUseCase;

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @GetMapping("/stats")
    public ResponseEntity<GetGamificationStatsDTO.GetGamificationStatsResponse> getStats(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(getGamificationStatsUseCase.execute(userId));
    }

    @PostMapping("/xp")
    public ResponseEntity<AddXpDTO.AddXpResponse> addXp(
            @PathVariable UUID userId,
            @RequestBody AddXpDTO.AddXpRequest request) {
        return ResponseEntity.ok(addXpUseCase.execute(userId, request));
    }

    @PostMapping("/streak")
    public ResponseEntity<UpdateStreakDTO.UpdateStreakResponse> updateStreak(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(updateStreakUseCase.execute(userId));
    }

    /**
     * Daily check-in: awards +10 XP once per calendar day.
     * Can be called safely multiple times — only awards once.
     */
    @PostMapping("/daily-checkin")
    public ResponseEntity<GetGamificationStatsDTO.GetGamificationStatsResponse> dailyCheckin(
            @PathVariable UUID userId) {
        // Ensure stats exist
        getGamificationStatsUseCase.execute(userId);

        LocalDate today = LocalDate.now();
        LocalDate lastDate = gamificationStatsRepository.getLastLoginXpDate(userId);

        if (lastDate == null || !lastDate.equals(today)) {
            gamificationStatsRepository.updateXpAndLevel(userId, DAILY_LOGIN_XP);
            gamificationStatsRepository.setLastLoginXpDate(userId, today);
        }

        return ResponseEntity.ok(getGamificationStatsUseCase.execute(userId));
    }
}
