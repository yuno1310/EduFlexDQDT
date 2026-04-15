package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.GetBadgeDTO.CourseBadgeResponse;
import com.eduflex.repository.BadgeRepository;

import java.util.UUID;

@Service
public class GetCourseBadgeUseCase {

  @Autowired
  private BadgeRepository badgeRepository;

  public CourseBadgeResponse execute(UUID userId, UUID courseId) {
    var record = badgeRepository.getEarnedCourseBadge(userId, courseId);

    if (record == null) {
      return new CourseBadgeResponse(false, "Failed to get badge", null, null, null, null);
    }

    return new CourseBadgeResponse(
        true,
        "Successful",
        record.badgeName(),
        record.description(),
        record.iconUrl(),
        record.earnedAt());
  }
}
