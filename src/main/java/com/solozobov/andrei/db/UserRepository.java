package com.solozobov.andrei.db;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
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

  public UsersRecord getById(long userId) {
    return db.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchOne();
  }

  public @NotNull UsersRecord getByLogin(@NotNull String login) {
    return db.selectFrom(USERS).where(USERS.LOGIN.eq(login)).fetchOne();
  }

  public @NotNull UsersRecord getOrCreateOrUpdateUser(@NotNull Chat chat) {
    return getOrCreateOrUpdateUser(chat.getId(), chat.getUserName(), chat.getFirstName(), chat.getLastName());
  }

  @VisibleForTesting @NotNull UsersRecord getOrCreateOrUpdateUser(long chatId, String login, String firstName, String lastName) {
    UsersRecord cached = login2user.get(login);

    if (cached != null) {
      cached = updateMetadata(cached, chatId, login, firstName, lastName);
      return cached;
    }

    UsersRecord existing = getByChatId(chatId);
    if (existing != null) {
      existing = updateMetadata(existing, chatId, login, firstName, lastName);
      login2user.put(login, existing);
      return existing;
    }

    final UsersRecord created = db.insertInto(USERS, USERS.CHAT_ID, USERS.LOGIN, USERS.FIRST_NAME, USERS.LAST_NAME, USERS.APPROVED)
      .values(chatId, login, firstName, lastName, false)
      .onDuplicateKeyIgnore()
      .returning()
      .fetchOne();
    login2user.put(login, created);
    LOG.info("New user registered " + login);

    return created;
  }

  public void changeApprove(long userId, boolean approve) {
    db.update(USERS)
      .set(USERS.APPROVED, approve)
      .where(USERS.ID.eq(userId))
      .execute();
    final UsersRecord user = getById(userId);
    login2user.put(user.getLogin(), user);
  }

  @VisibleForTesting @NotNull UsersRecord updateMetadata(
      @NotNull UsersRecord user,
      long chatId,
      String login,
      String firstName,
      String lastName
  ) {
    final boolean chatIdChanged = !((Long) chatId).equals(user.getChatId());
    final boolean firstNameChanged = firstName != null && !firstName.equals(user.getFirstName());
    final boolean lastNameChanged = lastName != null && !lastName.equals(user.getLastName());
    if (chatIdChanged || firstNameChanged || lastNameChanged) {
      UpdateSetMoreStep<UsersRecord> update = db.update(USERS).set(USERS.CHAT_ID, chatId);
      if (firstNameChanged) {
        update = update.set(USERS.FIRST_NAME, firstName);
      }
      if (lastNameChanged) {
        update = update.set(USERS.LAST_NAME, lastName);
      }
      update.where(USERS.ID.eq(user.getId())).execute();
      final UsersRecord updated = getById(user.getId());
      login2user.put(login, updated);
      return updated;
    } else {
      return user;
    }
  }

  private UsersRecord getByChatId(long chatId) {
    return db.selectFrom(USERS).where(USERS.CHAT_ID.eq(chatId)).fetchOne();
  }

  @Autowired
  public UserRepository(DSLContext db) {
    this.db = db;
  }
}
