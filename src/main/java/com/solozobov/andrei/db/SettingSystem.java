package com.solozobov.andrei.db;

import com.solozobov.andrei.utils.cache.CacheRegistry;
import com.solozobov.andrei.utils.cache.NamedLoadingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * solozobov on 05.11.2019
 */
@Component
public class SettingSystem {
  private static final Logger LOG = LoggerFactory.getLogger(SettingSystem.class);
  private static final Object NULL = new Object();

  private final SettingRepository settingRepository;

  private final NamedLoadingCache<SettingKey, Object> key2value;

  @Nullable
  public <T> T get(@NotNull SettingKey<T> key) {
    final Object value = key2value.get(key);
    if (NULL == value) {
      return null;
    } else {
      //noinspection unchecked
      return (T) value;
    }
  }

  private <T> Object load(@NotNull SettingKey<T> key) {
    final String value = settingRepository.get(key.key);
    if (value != null) {
      return key.valueConverter.reverse(value);
    }

    if (key.defaultValue == null) {
      LOG.info("Setting " + key.key + " set to default 'NULL'");
      return NULL;
    }

    final String defaultValue = key.valueConverter.convert(key.defaultValue);
    settingRepository.persistOrUpdate(key.key, defaultValue);
    LOG.info("Setting " + key.key + " set to default '" + defaultValue + "'");
    return key.defaultValue;
  }

  public <T> void set(@NotNull SettingKey<T> key, @Nullable T value) {
    if (value == null) {
      settingRepository.delete(key.key);
      key2value.put(key, NULL);
    } else {
      settingRepository.persistOrUpdate(key.key, key.valueConverter.convert(value));
      key2value.put(key, value);
    }
  }

  @Autowired
  public SettingSystem(
      SettingRepository settingRepository,
      CacheRegistry cacheRegistry
  ) {
    this.settingRepository = settingRepository;
    key2value = cacheRegistry.reloadingCache("systemSettings", 1000, 5, SettingKey::get, this::load);
  }
}
