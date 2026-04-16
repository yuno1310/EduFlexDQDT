package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.UsersDbO;
import com.eduflex.generated.tables.Users;
import com.eduflex.generated.tables.records.UsersRecord;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {
  @Autowired
  private DSLContext dsl;

  public boolean save(UsersDbO user) {
    user.record.attach(dsl.configuration());
    if (user.record.store() > 0) {
      return true;
    } else {
      return false;
    }
  }

  public UsersDbO find_by_email(String email) {
    var record = dsl.selectFrom(Users.USERS).where(Users.USERS.EMAIL.eq(email)).limit(1).fetchOne();
    if (record != null) {
      return new UsersDbO(record);
    } else {
      return null;
    }
  }

  public UsersDbO find_by_id(UUID userId) {
    var record = dsl.selectFrom(Users.USERS)
        .where(Users.USERS.USER_ID.eq(userId))
        .limit(1)
        .fetchOne();
    return record != null ? new UsersDbO(record) : null;
  }

  public UsersRecord find_by_id_record(UUID userID) {
    return dsl.selectFrom(Users.USERS).where(Users.USERS.USER_ID.eq(userID)).fetchOne();
  }

  public boolean updateInfoUser(UsersRecord record) {
    int updateRow = dsl.update(Users.USERS).set(Users.USERS.FULL_NAME, record.getFullName())
        .set(Users.USERS.PASSWORD_HASH, record.getPasswordHash()).where(Users.USERS.USER_ID.eq(record.getUserId()))
        .execute();
    return updateRow > 0;
  }

  /**
   * Get all users ordered by creation date (newest first).
   */
  public List<UsersDbO> findAll() {
    return dsl.selectFrom(Users.USERS)
        .orderBy(Users.USERS.CREATED_AT.desc())
        .fetch()
        .map(UsersDbO::new);
  }

  /**
   * Delete a user by ID. Returns true if deleted.
   */
  public boolean deleteById(UUID userId) {
    int rows = dsl.deleteFrom(Users.USERS)
        .where(Users.USERS.USER_ID.eq(userId))
        .execute();
    return rows > 0;
  }
}

