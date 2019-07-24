package com.solozobov.andrei.db;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.db.tables.records.UsersRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.HashMap;
import java.util.Map;

import static org.jooq.db.tables.Users.USERS;

/**
 * solozobov on 08/01/2019
 */

// https://www.jooq.org/doc/3.11/manual/sql-building/
@Repository
public class UserRepository {
  private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

  private final DSLContext db;

  private final Map<String,UsersRecord> login2user = new HashMap<>();

  public @NotNull UsersRecord createOrUpdateUser(Chat chat) {
    final String login = chat.getUserName();
    UsersRecord existing = login2user.get(login);
    if (existing == null) {
      existing = getOrCreate(chat);
      login2user.put(login, existing);
    } else {
      if (chat.getId() != null && !chat.getId().equals(existing.getChatId())
       || chat.getFirstName() != null && !chat.getFirstName().equals(existing.getFirstName())
       || chat.getLastName() != null && !chat.getLastName().equals(existing.getLastName())
      ) {
        existing = db.update(USERS)
          .set(USERS.CHAT_ID, chat.getId())
          .set(USERS.FIRST_NAME, chat.getFirstName())
          .set(USERS.LAST_NAME, chat.getLastName())
          .where(USERS.CHAT_ID.eq(chat.getId()))
          .returning()
          .fetchOne();
        login2user.put(login, existing);
      }
    }

    return existing;
  }

  public void approve(long userId) {
    db.update(USERS)
      .set(USERS.APPROVED, true)
      .where(USERS.ID.eq(userId))
      .returning()
      .fetchOne();
    final UsersRecord user = get(userId);
    login2user.put(user.getLogin(), user);
  }

  private @NotNull UsersRecord getOrCreate(Chat chat) {
    final UsersRecord existing = get(chat);
    if (existing != null) {
      return existing;
    }
    db.insertInto(USERS, USERS.CHAT_ID, USERS.LOGIN, USERS.FIRST_NAME, USERS.LAST_NAME, USERS.APPROVED)
      .values(chat.getId(), chat.getUserName(), chat.getFirstName(), chat.getLastName(), false)
      .onDuplicateKeyIgnore()
      .execute();
    LOG.info("New user registered " + chat);
    return get(chat);
  }

  private UsersRecord get(long userId) {
    return db.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchOne();
  }

  private UsersRecord get(Chat chat) {
    return db.selectFrom(USERS).where(USERS.CHAT_ID.eq(chat.getId())).fetchOne();
  }

  @Autowired
  public UserRepository(DSLContext db) {
    this.db = db;
  }
}
