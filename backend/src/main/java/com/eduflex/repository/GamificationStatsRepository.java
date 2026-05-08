package com.eduflex.repository;

import com.eduflex.dto.LeaderBoardDTO.LeaderBoardUserInfo;
import com.eduflex.entity.GamificationStatsDbO;
import com.eduflex.generated.tables.GamificationStats;
import com.eduflex.generated.tables.Users;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class GamificationStatsRepository {

  @Autowired
  private DSLContext dsl;

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

  public void updateXpAndLevel(UUID userId, int amount) {
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.XP,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount))
        .set(GamificationStats.GAMIFICATION_STATS.LEVEL,
            GamificationStats.GAMIFICATION_STATS.XP.plus(amount).div(100).plus(1))
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .execute();
  }

  public void updateStreak(UUID userId, int newStreak, LocalDate today) {
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.STREAK_DAYS, newStreak)
        .set(GamificationStats.GAMIFICATION_STATS.LAST_STUDY_DATE, today)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .execute();
  }

  public LocalDate getLastLoginXpDate(UUID userId) {
    return dsl.select(GamificationStats.GAMIFICATION_STATS.LAST_LOGIN_XP_DATE)
        .from(GamificationStats.GAMIFICATION_STATS)
        .where(GamificationStats.GAMIFICATION_STATS.USER_ID.eq(userId))
        .fetchOneInto(LocalDate.class);
  }

  public void setLastLoginXpDate(UUID userId, LocalDate date) {
    dsl.update(GamificationStats.GAMIFICATION_STATS)
        .set(GamificationStats.GAMIFICATION_STATS.LAST_LOGIN_XP_DATE, date)
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
}
