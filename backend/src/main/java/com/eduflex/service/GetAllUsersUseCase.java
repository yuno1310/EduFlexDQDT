package com.eduflex.service;

import com.eduflex.dto.AdminDTO.AdminUserItem;
import com.eduflex.dto.AdminDTO.GetAllUsersResponse;
import com.eduflex.entity.UsersDbO;
import com.eduflex.repository.GamificationStatsRepository;
import com.eduflex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GetAllUsersUseCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    public GetAllUsersResponse execute() {
        List<UsersDbO> users = userRepository.findAll();

        if (users == null || users.isEmpty()) {
            return new GetAllUsersResponse(true, "No users found", new ArrayList<>());
        }

        List<AdminUserItem> items = new ArrayList<>();
        for (UsersDbO user : users) {
            var record = user.record;
            int xp = 0, level = 0, streak = 0;
            String lastStudyDate = null;

            // Try to get gamification stats
            var stats = gamificationStatsRepository.findByUserId(record.getUserId());
            if (stats != null) {
                xp = stats.record.getXp();
                level = stats.record.getLevel();
                streak = stats.record.getStreakDays();
                var lsd = stats.record.getLastStudyDate();
                lastStudyDate = lsd != null ? lsd.toString() : null;
            }

            String role = record.getRole();
            if (role == null) role = "user";

            items.add(new AdminUserItem(
                    record.getUserId(),
                    record.getEmail(),
                    record.getFullName(),
                    record.getAvatarUrl(),
                    record.getCreatedAt(),
                    xp,
                    level,
                    streak,
                    lastStudyDate,
                    role
            ));
        }

        return new GetAllUsersResponse(true, "Users retrieved successfully", items);
    }
}
