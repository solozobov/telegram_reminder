package com.solozobov.andrei;

import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.SettingSystem;
import com.solozobov.andrei.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * solozobov on 26.11.2019
 */
public class TestTelegramBot extends TelegramBot {
  public TestTelegramBot(SettingSystem settingSystem) {
    super(new TestTelegramBotsApi(), settingSystem, "test_bot", "test_token", false);
  }

  private final List<Pair<Long, Integer>> actuallyForwarded = new ArrayList<>();

  @Override
  public void forward(long chatId, int messageId) {
    actuallyForwarded.add(Pair.of(chatId, messageId));
  }

  public void reset() {
    actuallyForwarded.clear();
  }

  public void assertForwarded(long chatId, int messageId) {
    assertForwarded(Pair.of(chatId, messageId));
  }

  @SafeVarargs
  public final void assertForwarded(Pair<Long, Integer> ... expected) {
    for (Pair<Long, Integer> pair : expected) {
      assertTrue(actuallyForwarded.contains(pair));
    }
  }
}
