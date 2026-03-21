package com.eduflex.service;

import com.eduflex.dto.GetUserBadgesDTO;
import com.eduflex.repository.BadgeRepository;
import com.eduflex.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetUserBadgesUseCase {

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    public List<GetUserBadgesDTO.GetUserBadgesResponse> execute(UUID userId) {
        return userBadgeRepository.findByUserId(userId).stream()
                .map(ub -> {
                    var badge = badgeRepository.findById(ub.record.getBadgeId());
                    return new GetUserBadgesDTO.GetUserBadgesResponse(
                            ub.record.getId(),
                            ub.record.getUserId(),
                            ub.record.getBadgeId(),
                            badge != null ? badge.record.getName() : null,
                            badge != null ? badge.record.getDescription() : null,
                            badge != null ? badge.record.getIconUrl() : null,
                            ub.record.getEarnedAt()
                    );
                })
                .toList();
    }
}
