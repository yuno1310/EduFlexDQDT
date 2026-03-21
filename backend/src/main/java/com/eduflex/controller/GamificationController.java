package com.eduflex.controller;

import com.eduflex.dto.AddXpDTO;
import com.eduflex.dto.GetGamificationStatsDTO;
import com.eduflex.dto.UpdateStreakDTO;
import com.eduflex.service.AddXpUseCase;
import com.eduflex.service.GetGamificationStatsUseCase;
import com.eduflex.service.UpdateStreakUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}")
public class GamificationController {

    @Autowired
    private GetGamificationStatsUseCase getGamificationStatsUseCase;

    @Autowired
    private AddXpUseCase addXpUseCase;

    @Autowired
    private UpdateStreakUseCase updateStreakUseCase;

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
}
