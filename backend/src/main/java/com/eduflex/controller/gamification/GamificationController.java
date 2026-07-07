package com.eduflex.controller.gamification;

import com.eduflex.dto.gamification.AddXpDTO;
import com.eduflex.dto.gamification.GetGamificationStatsDTO;
import com.eduflex.dto.gamification.UpdateStreakDTO;
import com.eduflex.dto.gamification.LeaderBoardDTO.GetLeaderBoardResponse;
import com.eduflex.service.gamification.AddXpUseCase;
import com.eduflex.service.gamification.DailyCheckinUseCase;
import com.eduflex.service.gamification.GetGamificationStatsUseCase;
import com.eduflex.service.gamification.GetLeaderBoardUseCase;
import com.eduflex.service.gamification.UpdateStreakUseCase;
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

  @Autowired
  private DailyCheckinUseCase dailyCheckinUseCase;

  @Autowired
  private GetLeaderBoardUseCase getLeaderBoardUseCase;

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

  @PostMapping("/daily-checkin")
  public ResponseEntity<GetGamificationStatsDTO.GetGamificationStatsResponse> dailyCheckin(
      @PathVariable UUID userId) {
    return ResponseEntity.ok(dailyCheckinUseCase.execute(userId));
  }

  @GetMapping("/leaderboard")
  public ResponseEntity<GetLeaderBoardResponse> getLeaderBoard(@RequestParam(defaultValue = "50") int limit) {
    var response = getLeaderBoardUseCase.execute(limit);
    return ResponseEntity.ok(response);
  }
}
