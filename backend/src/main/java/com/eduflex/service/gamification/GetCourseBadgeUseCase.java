package com.eduflex.service.gamification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.gamification.GetBadgeDTO.CourseBadgeResponse;
import com.eduflex.repository.gamification.BadgeRepository;

import java.util.UUID;

@Service
public class GetCourseBadgeUseCase {

  @Autowired
  private BadgeRepository badgeRepository;

  public CourseBadgeResponse execute(UUID userId, UUID courseId) {
    return badgeRepository.getEarnedCourseBadge(userId, courseId);
  }
}
