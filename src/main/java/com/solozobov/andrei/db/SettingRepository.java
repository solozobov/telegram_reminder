package com.solozobov.andrei.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static org.jooq.db.tables.Settings.SETTINGS;

/**
 * solozobov on 05.11.2019
 */
@Repository
public class SettingRepository {

  private final DSLContext db;

  public void set(@NotNull String key, @NotNull String value) {
    if (db.update(SETTINGS).set(SETTINGS.VALUE, value).where(SETTINGS.KEY.eq(key)).execute() == 0) {
      db.insertInto(SETTINGS, SETTINGS.KEY, SETTINGS.VALUE).values(key, value).execute();
    }
  }

  public @Nullable String get(@NotNull String key) {
    final Record1<String> record = db.select(SETTINGS.VALUE).from(SETTINGS).where(SETTINGS.KEY.eq(key)).fetchOne();
    return record == null ? null : record.get(SETTINGS.VALUE);
  }

  public void delete(@NotNull String key) {
    db.delete(SETTINGS).where(SETTINGS.KEY.eq(key)).execute();
  }

  @Autowired
  public SettingRepository(DSLContext db) {
    this.db = db;
  }
}
