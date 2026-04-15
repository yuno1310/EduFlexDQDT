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
    return badgeRepository.getEarnedCourseBadge(userId, courseId);
  }
}
