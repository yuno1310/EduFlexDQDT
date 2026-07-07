package com.eduflex.service.gamification;

import com.eduflex.entity.gamification.UserBadgesDbO;
import com.eduflex.entity.gamification.BadgesDbO;
import com.eduflex.generated.tables.records.BadgesRecord;
import com.eduflex.repository.gamification.BadgeRepository;
import com.eduflex.repository.course.CourseRepository;
import com.eduflex.repository.gamification.GamificationStatsRepository;
import com.eduflex.repository.gamification.UserBadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Automatically checks and awards badges based on user actions.
 * Called from: LogInUseCase, AddXpUseCase, UpdateStreakUseCase.
 */
@Service
public class CheckAndAwardBadgesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CheckAndAwardBadgesUseCase.class);

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private GamificationStatsRepository gamificationStatsRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Award a badge if user doesn't already have it.
     */
    private void tryAward(UUID userId, String conditionType) {
        var badge = badgeRepository.findByConditionType(conditionType);
        if (badge == null) {
            log.warn("No badge found for condition: {}", conditionType);
            return;
        }
        Long badgeId = badge.record.getId();
        if (!userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            userBadgeRepository.save(new UserBadgesDbO(userId, badgeId));
            log.info("Awarded badge '{}' to user {}", badge.record.getName(), userId);
        }
    }

    /**
     * Called after login — awards FIRST_LOGIN badge.
     */
    @Transactional
    public void checkLoginBadge(UUID userId) {
        tryAward(userId, "FIRST_LOGIN");
    }

    /**
     * Called after streak update — checks STREAK_7 and STREAK_30.
     */
    @Transactional
    public void checkStreakBadges(UUID userId, int currentStreak) {
        if (currentStreak >= 7) {
            tryAward(userId, "STREAK_7");
        }
        if (currentStreak >= 30) {
            tryAward(userId, "STREAK_30");
        }
    }

    /**
     * Called after XP update — checks XP_500 and XP_1000.
     */
    @Transactional
    public void checkXpBadges(UUID userId) {
        var stats = gamificationStatsRepository.findByUserId(userId);
        if (stats == null) return;

        int xp = stats.record.getXp();
        if (xp >= 500) {
            tryAward(userId, "XP_500");
        }
        if (xp >= 1000) {
            tryAward(userId, "XP_1000");
        }
    }

    /**
     * Called when a course is completed.
     * Generates a badge dynamically if it does not exist for this course yet, and awards it.
     */
    @Transactional
    public void checkCourseCompletionBadge(UUID userId, UUID courseId) {
        String conditionType = "COURSE_" + courseId.toString();
        var badge = badgeRepository.findByConditionType(conditionType);

        if (badge == null) {
            var course = courseRepository.find_by_id_course(courseId);
            if (course == null) {
                log.warn("Cannot create completion badge, course not found: {}", courseId);
                return;
            }

            BadgesRecord record = new BadgesRecord();
            record.setName("Certified: " + course.getTitle());
            record.setDescription("Successfully completed the course: " + course.getTitle());
            record.setConditionType(conditionType);

            badgeRepository.save(new BadgesDbO(record));
            log.info("Dynamically created new badge for course: {}", course.getTitle());
        }

        tryAward(userId, conditionType);
    }
}
