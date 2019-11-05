package com.solozobov.andrei.utils.cache;

import com.google.common.cache.LoadingCache;
import com.solozobov.andrei.RememberException;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * solozobov on 05.11.2019
 */
public class NamedLoadingCache<K, V> extends NamedCache<K, V>{
  private final LoadingCache<K, V> cache;

  NamedLoadingCache(String name, Function<String, K> keyDeserializer, LoadingCache<K, V> cache) {
    super(name, keyDeserializer, cache);
    this.cache = cache;
  }

  public @NotNull V get(K key) {
    try {
      return cache.get(key);
    } catch (Exception e) {
      throw new RememberException(e, "Can't get value from cache '%s' by key '%s'", name, key);
    }
  }
}
