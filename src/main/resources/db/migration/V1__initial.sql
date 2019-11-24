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
  forestall_minutes INT       NOT NULL,
  repeat_minutes    INT       NULL
);

CREATE TABLE IF NOT EXISTS settings(
  id      BIGSERIAL NOT NULL PRIMARY KEY,
  key     VARCHAR   NOT NULL,
  value   VARCHAR   NOT NULL,

  CONSTRAINT settings_ui_key UNIQUE(key),

  CONSTRAINT settings_chk_key CHECK(length(key) <= 1024),
  CONSTRAINT settings_chk_value CHECK(length(value) <= 4096)
);
