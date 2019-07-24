CREATE TABLE IF NOT EXISTS users(
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  chat_id    BIGINT    NOT NULL,
  login      VARCHAR,
  first_name VARCHAR,
  last_name  VARCHAR,
  approved   BOOLEAN DEFAULT FALSE,

  CONSTRAINT users_ui_chat_id UNIQUE(chat_id),
  CONSTRAINT users_ui_login UNIQUE(login)
);

CREATE TABLE IF NOT EXISTS notifications(
  id                BIGSERIAL NOT NULL PRIMARY KEY,
  chat_id           BIGINT    NOT NULL,
  message_id        INT       NOT NULL,
  timestamp_utc     TIMESTAMP NOT NULL,
  forestall_minutes INT       NOT NULL
);
