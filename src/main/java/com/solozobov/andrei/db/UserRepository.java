package com.solozobov.andrei.db;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Chat;

import static org.jooq.db.tables.Users.USERS;

/**
 * solozobov on 08/01/2019
 */
@Repository
public class UserRepository {
  private Logger LOG = LoggerFactory.getLogger(UserRepository.class);

  private final DSLContext db;

  public void createOrUpdateUser(Chat chat) {
    final int updates = db.insertInto(USERS, USERS.CHAT_ID, USERS.LOGIN, USERS.FIRST_NAME, USERS.LAST_NAME)
      .values(chat.getId(), chat.getUserName(), chat.getFirstName(), chat.getLastName())
      .onDuplicateKeyIgnore()
      .execute();
    if (updates > 0) {
      LOG.info("User info updated: " + chat.getUserName() + "@ (" + chat.getFirstName() + " " + chat.getLastName() + ") chat#" + chat.getId());
    }
  }

  @Autowired
  public UserRepository(DSLContext db) {
    this.db = db;
  }
}
