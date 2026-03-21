package com.eduflex.controller;

import com.eduflex.dto.GamificationStatsDTO;
import com.eduflex.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    /**
     * GET /api/users/{userId}/stats
     * Get gamification stats (XP, level, streak) for a user
     */
    @GetMapping("/stats")
    public ResponseEntity<GamificationStatsDTO> getStats(@PathVariable String userId) {
        return ResponseEntity.ok(gamificationService.getStatsByUserId(userId));
    }

    /**
     * POST /api/users/{userId}/xp
     * Add XP to a user. Body: { "amount": 10 }
     */
    @PostMapping("/xp")
    public ResponseEntity<GamificationStatsDTO> addXp(
            @PathVariable String userId,
            @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 0);
        return ResponseEntity.ok(gamificationService.addXp(userId, amount));
    }

    /**
     * POST /api/users/{userId}/streak
     * Update daily streak for a user
     */
    @PostMapping("/streak")
    public ResponseEntity<GamificationStatsDTO> updateStreak(@PathVariable String userId) {
        return ResponseEntity.ok(gamificationService.updateStreak(userId));
    }
}
