package com.eduflex.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.entity.UsersDbO;
import com.eduflex.generated.tables.Users;

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

}
