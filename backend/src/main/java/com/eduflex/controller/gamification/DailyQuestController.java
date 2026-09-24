package com.eduflex.controller.gamification;

import com.eduflex.dto.gamification.DailyQuestDTO.DailyQuestResponse;
import com.eduflex.dto.gamification.DailyQuestDTO.QuestProgressRequest;
import com.eduflex.service.gamification.GetDailyQuestsUseCase;
import com.eduflex.service.gamification.UpdateDailyQuestProgressUseCase;
import com.eduflex.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/daily-quests")
public class DailyQuestController {

    @Autowired
    private GetDailyQuestsUseCase getDailyQuestsUseCase;

    @Autowired
    private UpdateDailyQuestProgressUseCase updateDailyQuestProgressUseCase;

    @GetMapping
    public ResponseEntity<List<DailyQuestResponse>> getDailyQuests(
            @PathVariable UUID userId, Authentication authentication) {
        AuthenticatedUser.requireOwner(authentication, userId);
        return ResponseEntity.ok(getDailyQuestsUseCase.execute(userId));
    }

    @PostMapping("/progress")
    public ResponseEntity<Void> reportProgress(
            @PathVariable UUID userId,
            @RequestBody QuestProgressRequest request,
            Authentication authentication) {
        AuthenticatedUser.requireOwner(authentication, userId);
        updateDailyQuestProgressUseCase.execute(userId, request.questType(), request.increment());
        return ResponseEntity.ok().build();
    }
}
