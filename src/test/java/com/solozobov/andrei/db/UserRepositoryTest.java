package com.solozobov.andrei.db;

import com.solozobov.andrei.DomainTestCase;
import org.jooq.db.tables.records.UsersRecord;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * solozobov on 30.10.2020
 */
public class UserRepositoryTest extends DomainTestCase {

  @Test
  public void changeApprove() {
    final String login = rUniqString();
    final Long chatId = rLong();
    final String firstName = rString();
    final String lastName = rString();
    final UsersRecord created = userRepository.getOrCreateOrUpdateUser(chatId, login, firstName, lastName);
    check(created, login, chatId, firstName, lastName, false);

    userRepository.changeApprove(created.getId(), true);
    check(userRepository.getById(created.getId()), login, chatId, firstName, lastName, true);

    userRepository.changeApprove(created.getId(), false);
    check(userRepository.getById(created.getId()), login, chatId, firstName, lastName, false);

    userRepository.changeApprove(created.getId(), false);
    check(userRepository.getById(created.getId()), login, chatId, firstName, lastName, false);
  }

  @Test
  public void getOrCreateOrUpdateUser() {
    final String login = rUniqString();
    final Long chatId = rLong();
    final String firstName = rString();
    final String lastName = rString();
    final UsersRecord created = userRepository.getOrCreateOrUpdateUser(chatId, login, firstName, lastName);
    check(created, login, chatId, firstName, lastName, false);

    final UsersRecord same = userRepository.getOrCreateOrUpdateUser(chatId, login, firstName, lastName);
    check(same, login, chatId, firstName, lastName, false);

    final Long chatId2 = rLong();
    final UsersRecord updatedChatId = userRepository.getOrCreateOrUpdateUser(chatId2, login, firstName, lastName);
    check(updatedChatId, login, chatId2, firstName, lastName, false);

    final String firstName2 = rString();
    final UsersRecord updatedFirstName = userRepository.getOrCreateOrUpdateUser(chatId2, login, firstName2, lastName);
    check(updatedFirstName, login, chatId2, firstName2, lastName, false);

    final String lastName2 = rString();
    final UsersRecord updatedLastName = userRepository.getOrCreateOrUpdateUser(chatId2, login, firstName2, lastName2);
    check(updatedLastName, login, chatId2, firstName2, lastName2, false);

    final Long chatId3 = rLong();
    final String firstName3 = rString();
    final String lastName3 = rString();
    final UsersRecord updatedEverything = userRepository.getOrCreateOrUpdateUser(chatId3, login, firstName3, lastName3);
    check(updatedEverything, login, chatId3, firstName3, lastName3, false);
  }

  private void check(
      UsersRecord actual,
      String expectedLogin,
      Long expectedChatId,
      String expectedFirstName,
      String expectedLastName,
      boolean expectedApproved
  ) {
    assertEquals(expectedChatId, actual.getChatId());
    assertEquals(expectedLogin, actual.getLogin());
    assertEquals(expectedFirstName, actual.getFirstName());
    assertEquals(expectedLastName, actual.getLastName());
    assertEquals(expectedApproved, actual.getApproved());
  }
}
