package com.solozobov.andrei.bot;

import com.solozobov.andrei.RememberException;
import com.solozobov.andrei.utils.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * solozobov on 07.07.2019
 */
public abstract class ButtonAction<Data> {
  private static final Logger LOG = LoggerFactory.getLogger(ButtonAction.class);
  public static final Set<ButtonAction> actions = new HashSet<>();

  private final String key;
  private final Serializer<Data> serializer;

  public String getActionKey(Data data) {
    return key + " " + serializer.serialize(data);
  }

  public boolean accepts(String actionKey) {
    final int index = actionKey.indexOf(' ');
    return index >= 0 && key.equals(actionKey.substring(0, index));
  }

  public void perform(TelegramBot bot, Message message, String actionKey) {
    final Chat chat = message.getChat();
    final String userName = chat.getUserName() + "@ " + chat.getFirstName() + " " + chat.getLastName();
    LOG.info(userName + " pressed Button '" + actionKey + "'");
    perform2(bot, message, serializer.deserialize(actionKey.substring(key.length() + 1)));
  }

  protected abstract void perform2(TelegramBot bot, Message message, Data data);

  public ButtonAction(String key, Serializer<Data> serializer) {
    this.key = key;
    this.serializer = serializer;
    if (!actions.add(this)) {
      throw new RememberException("Duplicated ButtonAction for key '" + key + "'");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (!(o instanceof ButtonAction)) { return false; }
    final ButtonAction that = (ButtonAction) o;
    return key.equals(that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key);
  }
}
