package com.eduflex.repository.payment;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eduflex.generated.tables.Transactions;
import java.util.UUID;

@Repository
public class TransactionRepository {

  @Autowired
  private DSLContext dsl;

  public boolean saveSuccessfulTransaction(UUID userId, UUID courseId, long amount) {
    int insertedRows = dsl.insertInto(Transactions.TRANSACTIONS)
        .set(Transactions.TRANSACTIONS.USER_ID, userId)
        .set(Transactions.TRANSACTIONS.COURSE_ID, courseId)
        .set(Transactions.TRANSACTIONS.AMOUNT, amount)
        .set(Transactions.TRANSACTIONS.PAYMENT_METHOD, "MOMO")
        .set(Transactions.TRANSACTIONS.STATUS, "SUCCESS")
        .execute();

    return insertedRows > 0;
  }
}
