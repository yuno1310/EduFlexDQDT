package com.eduflex.entity;

import java.time.OffsetDateTime;

import com.eduflex.generated.tables.Users;
import com.eduflex.generated.tables.records.UsersRecord;

public class UsersDbO {
  public UsersRecord record;

  public UsersDbO(String userID, String email, String password, String fullName, OffsetDateTime createdAt, boolean active) {
    record = Users.USERS.newRecord();
    record.setUserId(userID);
    record.setEmail(email);
    record.setPasswordHash(password);
    record.setFullName(fullName);
    record.setCreatedAt(createdAt);
    record.setIsActive(active);
  }

  public UsersDbO(UsersRecord record) {
    this.record = record;
  }
}
