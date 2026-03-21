package com.eduflex.controller;

import com.eduflex.dto.BadgeDTO;
import com.eduflex.dto.UserBadgeDTO;
import com.eduflex.service.BadgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * GET /api/badges
     * Get all available badges
     */
    @GetMapping("/api/badges")
    public ResponseEntity<List<BadgeDTO>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    /**
     * GET /api/users/{userId}/badges
     * Get all badges earned by a user
     */
    @GetMapping("/api/users/{userId}/badges")
    public ResponseEntity<List<UserBadgeDTO>> getUserBadges(@PathVariable String userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }

    /**
     * POST /api/users/{userId}/badges/{badgeId}
     * Award a badge to a user
     */
    @PostMapping("/api/users/{userId}/badges/{badgeId}")
    public ResponseEntity<UserBadgeDTO> awardBadge(
            @PathVariable String userId,
            @PathVariable Long badgeId) {
        UserBadgeDTO awarded = badgeService.awardBadge(userId, badgeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(awarded);
    }
}
