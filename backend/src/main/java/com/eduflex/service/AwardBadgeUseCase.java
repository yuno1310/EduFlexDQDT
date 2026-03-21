package com.eduflex.service;

import com.eduflex.dto.AwardBadgeDTO;
import com.eduflex.entity.UserBadgesDbO;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.repository.BadgeRepository;
import com.eduflex.repository.UserBadgeRepository;
import com.eduflex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AwardBadgeUseCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Transactional
    public AwardBadgeDTO.AwardBadgeResponse execute(UUID userId, Long badgeId) {
        if (userRepository.find_by_id(userId) == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        if (badgeRepository.findById(badgeId) == null) {
            throw new ResourceNotFoundException("Badge not found with id: " + badgeId);
        }

        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            return new AwardBadgeDTO.AwardBadgeResponse(false, "Badge already awarded to this user");
        }

        userBadgeRepository.save(new UserBadgesDbO(userId, badgeId));
        return new AwardBadgeDTO.AwardBadgeResponse(true, "Badge awarded successfully");
    }
}
