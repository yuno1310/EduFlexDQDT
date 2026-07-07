package com.eduflex.controller.gamification;

import com.eduflex.dto.gamification.AwardBadgeDTO;
import com.eduflex.dto.gamification.GetAllBadgesDTO;
import com.eduflex.dto.gamification.GetBadgeDTO.CourseBadgeResponse;
import com.eduflex.dto.gamification.GetUserBadgesDTO;
import com.eduflex.service.gamification.AwardBadgeUseCase;
import com.eduflex.service.gamification.GetAllBadgesUseCase;
import com.eduflex.service.gamification.GetCourseBadgeUseCase;
import com.eduflex.service.gamification.GetUserBadgesUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BadgeController {

  @Autowired
  private GetAllBadgesUseCase getAllBadgesUseCase;

  @Autowired
  private GetUserBadgesUseCase getUserBadgesUseCase;

  @Autowired
  private AwardBadgeUseCase awardBadgeUseCase;

  @Autowired
  private GetCourseBadgeUseCase getCourseBadgeUseCase;

  @GetMapping("/badges")
  public ResponseEntity<List<GetAllBadgesDTO.GetAllBadgesResponse>> getAllBadges() {
    return ResponseEntity.ok(getAllBadgesUseCase.execute());
  }

  @GetMapping("/users/{userId}/badges")
  public ResponseEntity<List<GetUserBadgesDTO.GetUserBadgesResponse>> getUserBadges(
      @PathVariable UUID userId) {
    return ResponseEntity.ok(getUserBadgesUseCase.execute(userId));
  }

  @PostMapping("/users/{userId}/badges/{badgeId}")
  public ResponseEntity<AwardBadgeDTO.AwardBadgeResponse> awardBadge(
      @PathVariable UUID userId,
      @PathVariable Long badgeId) {
    var response = awardBadgeUseCase.execute(userId, badgeId);

    if (response.success()) {
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/badges/course-completion")
  public ResponseEntity<CourseBadgeResponse> getBadge(
      @RequestParam UUID userId,
      @RequestParam UUID courseId) {
    var response = getCourseBadgeUseCase.execute(userId, courseId);

    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
