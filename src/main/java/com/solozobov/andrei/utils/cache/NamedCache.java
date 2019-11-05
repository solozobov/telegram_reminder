package com.solozobov.andrei.utils.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.solozobov.andrei.RememberException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * solozobov on 05.11.2019
 */
public class NamedCache<K, V> {
  public final String name;
  private Cache<K, V> cache;
  private final Function<String, K> keyDeserializer;

  NamedCache(String name, Function<String, K> keyDeserializer, Cache<K, V> cache) {
    this.name = name;
    this.keyDeserializer = keyDeserializer;
    this.cache = cache;
  }

  public @NotNull V get(K key, Callable<V> valueLoader) {
    try {
      return cache.get(key, valueLoader);
    } catch (Exception e) {
      throw new RememberException(e, "Can't get value from cache '%s' by key '%s'", name, key);
    }
  }

  public @Nullable V getIfPresent(K key) {
    return cache.getIfPresent(key);
  }

  public @NotNull ImmutableMap<K, V> getAllPresent(Iterable<K> keys) {
    return cache.getAllPresent(keys);
  }

  public void put(@NotNull K key, @NotNull V value) {
    cache.put(key, value);
  }

  public void putAll(@NotNull Map<K, V> entries) {
    cache.putAll(entries);
  }

  void invalidate(@NotNull String key) {
    cache.invalidate(keyDeserializer.apply(key));
  }

  void invalidateAll() {
    cache.invalidateAll();
  }

  CacheStats getStatistics() {
    return cache.stats();
  }
}
