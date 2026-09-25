package com.eduflex.repository.gamification;

import com.eduflex.dto.gamification.LeaderBoardDTO.LeaderBoardUserInfo;
import com.eduflex.entity.gamification.GamificationStatsDbO;
import com.eduflex.exception.ResourceNotFoundException;
import com.eduflex.generated.tables.GamificationStats;
import com.eduflex.generated.tables.Users;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.eduflex.monitoring.EduFlexMetrics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class GamificationStatsRepository {

  @Autowired
  private DSLContext dsl;

  @Autowired
  private EduFlexMetrics metrics;

  public GamificationStatsDbO findByUserId(UUID userId) {
    var record = dsl.selectFrom(GamificationStats.GAMIFICATION_STATS)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .fetchOne();
    return record != null ? new GamificationStatsDbO(record) : null;
  }

  public boolean save(GamificationStatsDbO stats) {
    stats.record.attach(dsl.configuration());
    return stats.record.store() > 0;
  }

  /**
   * Creates the user's stats row if needed, then serializes all reward/progress
   * writes for that user on the same database row. Must run in a transaction.
   */
  public GamificationStatsDbO ensureAndLock(UUID userId) {
    return metrics.timeStatsLock(() -> ensureAndLockRecord(userId));
  }

  private GamificationStatsDbO ensureAndLockRecord(UUID userId) {
    dsl.execute(
        "INSERT INTO gamification_stats (user_id, xp, level, streak_days) "
            + "SELECT user_id, 0, 1, 0 FROM users WHERE user_id = ? "
            + "ON CONFLICT (user_id) DO NOTHING",
        userId);

    var record = dsl.selectFrom(GamificationStats.GAMIFICATION_STATS)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .forUpdate()
        .fetchOne();
    if (record == null) {
      throw new ResourceNotFoundException("User not found: " + userId);
    }
    return new GamificationStatsDbO(record);
  }

  public void updateXpAndLevel(UUID userId, int amount) {
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.XP,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount))
        .set(GamificationStats.GAMIFICATION_STATS.LEVEL,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount).div(100).plus(1))
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .execute();
  }

  public boolean awardDailyLoginXp(UUID userId, LocalDate today, int amount) {
    return dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.XP,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount))
        .set(GamificationStats.GAMIFICATION_STATS.LEVEL,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount).div(100).plus(1))
        .set(GamificationStats.GAMIFICATION_STATS.LAST_LOGIN_XP_DATE, today)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .and(GamificationStats.GAMIFICATION_STATS.LAST_LOGIN_XP_DATE.isNull()
            .or(GamificationStats.GAMIFICATION_STATS.LAST_LOGIN_XP_DATE.lt(today)))
        .execute() == 1;
  }

  public void updateStreak(UUID userId, int newStreak, LocalDate today) {
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.STREAK_DAYS, newStreak)
        .set(GamificationStats.GAMIFICATION_STATS.LAST_STUDY_DATE, today)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .execute();
  }

  public List<LeaderBoardUserInfo> getLeaderBoard(int top) {
    var records = dsl
        .select(Users.USERS.USER_ID, Users.USERS.FULL_NAME, GamificationStats.GAMIFICATION_STATS.XP,
            GamificationStats.GAMIFICATION_STATS.LEVEL)
        .from(Users.USERS).join(GamificationStats.GAMIFICATION_STATS)
        .on(Users.USERS.USER_ID.eq(GamificationStats.GAMIFICATION_STATS.USER_ID))
        .where(Users.USERS.ROLE.notEqualIgnoreCase("admin"))
        .orderBy(GamificationStats.GAMIFICATION_STATS.XP.desc()).limit(top).fetch();

    List<LeaderBoardUserInfo> list = new ArrayList<LeaderBoardUserInfo>();

    if (records != null && records.isNotEmpty()) {
      for (var record : records) {
        list.add(new LeaderBoardUserInfo(record.value1(), record.value2(), record.value3(), record.value4()));
      }
    }
    return list;
  }

  public LeaderBoardUserInfo getUserInfoById(UUID userId) {
    var record = dsl
        .select(Users.USERS.FULL_NAME, GamificationStats.GAMIFICATION_STATS.XP, GamificationStats.GAMIFICATION_STATS.LEVEL)
        .from(Users.USERS).join(GamificationStats.GAMIFICATION_STATS)
        .on(Users.USERS.USER_ID.eq(GamificationStats.GAMIFICATION_STATS.USER_ID))
        .where(Users.USERS.USER_ID.eq(userId))
        .fetchOne();

    if (record != null) {
      return new LeaderBoardUserInfo(userId, record.value1(), record.value2(), record.value3());
    }
    return null;
  }
}
