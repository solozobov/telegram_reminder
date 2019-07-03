CREATE TABLE users(
  id         BIGSERIAL NOT NULL PRIMARY KEY,
  chat_id    BIGINT    NOT NULL,
  login      TEXT,
  first_name TEXT,
  last_name  TEXT,

  CONSTRAINT users_ui_login UNIQUE(login)
);

CREATE TABLE notifications(
  id                BIGSERIAL NOT NULL PRIMARY KEY,
  chat_id           BIGINT    NOT NULL,
  message_id        INT       NOT NULL,
  time              TIMESTAMP NOT NULL,
  forestall_minutes INT       NOT NULL
);
