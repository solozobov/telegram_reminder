package com.solozobov.andrei.utils.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.solozobov.andrei.RememberException;
import com.solozobov.andrei.utils.CountingThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static com.google.common.cache.RemovalCause.SIZE;
import static com.solozobov.andrei.utils.Assert.notNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toMap;

/**
 * solozobov on 05.11.2019
 */
@SuppressWarnings("WeakerAccess")
@Component
public class CacheRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(CacheRegistry.class);

  private final Map<String, NamedCache> name2cache = new ConcurrentHashMap<>();

  public @NotNull Map<String, CacheStats> getStatistics() {
    return name2cache.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().getStatistics()));
  }

  public void invalidate(@NotNull String cacheName, @NotNull String key) {
    notNull(name2cache.get(cacheName), "No cache with name '" + cacheName + "'").invalidate(key);
    LOG.info("Invalidated cache '{}' key '{}'", cacheName, key);
  }

  public void invalidateAll(@NotNull String cacheName) {
    notNull(name2cache.get(cacheName), "No cache with name '" + cacheName + "'").invalidateAll();
    LOG.info("Invalidated cache '{}'", cacheName);
  }


  public <V> @NotNull NamedCache<String, V> cache(@NotNull String name, int size) {
    return cache(name, size, key -> key);
  }

  public <K, V> @NotNull NamedCache<K, V> cache(
      @NotNull String name,
      int size,
      @NotNull Function<String, K> keyDeserializer
  ) {
    return create(name, size, keyDeserializer, cb -> {});
  }

  public <V> @NotNull NamedCache<String, V> expiringCache(
      @NotNull String name,
      int size,
      int expirationTimeMinutes
  ) {
    return expiringCache(name, size, expirationTimeMinutes, key -> key);
  }

  public <K, V> @NotNull NamedCache<K, V> expiringCache(
      @NotNull String name,
      int size,
      int expirationTimeMinutes,
      @NotNull Function<String, K> keyDeserializer
  ) {
    return expiringCache(name, size, expirationTimeMinutes, MINUTES, keyDeserializer);
  }

  public <K, V> @NotNull NamedCache<K, V> expiringCache(
      @NotNull String name,
      int size,
      int expirationTime,
      @NotNull TimeUnit timeUnit,
      @NotNull Function<String, K> keyDeserializer
  ) {
    return create(name, size, keyDeserializer, cb -> cb.expireAfterWrite(expirationTime, timeUnit));
  }

  public <V> @NotNull NamedLoadingCache<String, V> reloadingCache(
      @NotNull String name,
      int size,
      int reloadIntervalMinutes,
      @NotNull Function<String, V> key2valueLoader
  ) {
    return reloadingCache(name, size, reloadIntervalMinutes, key -> key, key2valueLoader);
  }

  public <K, V> @NotNull NamedLoadingCache<K, V> reloadingCache(
      @NotNull String name,
      int size,
      int reloadIntervalMinutes,
      @NotNull Function<String, K> keyDeserializer,
      @NotNull Function<K,V> key2valueLoader
  ) {
    return reloadingCache(name, size, reloadIntervalMinutes, MINUTES, keyDeserializer, key2valueLoader);
  }

  public <K, V> @NotNull NamedLoadingCache<K, V> reloadingCache(
      @NotNull String name,
      int size,
      int reloadInterval,
      @NotNull TimeUnit timeUnit,
      @NotNull Function<String, K> keyDeserializer,
      @NotNull Function<K,V> key2valueLoader
  ) {
    final LoadingCache<K, V> cache = prepareCache(name, size)
        .refreshAfterWrite(reloadInterval, timeUnit)
        .build(new NsCacheLoader<>(name, key2valueLoader));
    final NamedLoadingCache<K, V> namedCache = new NamedLoadingCache<>(name, keyDeserializer, cache);
    if(name2cache.put(name, namedCache) != null) {
      throw new RememberException("Duplicated cache with name '%s'", name);
    }
    return namedCache;
  }

  private <K, V> @NotNull NamedCache<K, V> create(
      @NotNull String name,
      int size,
      @NotNull Function<String, K> keyDeserializer,
      @NotNull Consumer<CacheBuilder<K, V>> cacheConfigurator
  ) {
    final CacheBuilder<K, V> cacheBuilder = prepareCache(name, size);
    cacheConfigurator.accept(cacheBuilder);
    final NamedCache<K, V> namedCache = new NamedCache<>(name, keyDeserializer, cacheBuilder.build());
    if(name2cache.put(name, namedCache) != null) {
      throw new RememberException("Duplicated cache with name '%s'", name);
    }
    return namedCache;
  }

  private <K, V> CacheBuilder<K, V> prepareCache(@NotNull String name, int size) {
    return CacheBuilder
        .newBuilder()
        .initialCapacity(size)
        .maximumSize(size)
        .recordStats()
        .removalListener(n -> {
          if (n.getCause() == SIZE) {
            LOG.warn("Cache '" + name + "' size " + size + " overflow");
          }
        });  }

  private class NsCacheLoader<K, V> extends CacheLoader<K, V> {
    private final Function<K, V> key2valueLoader;
    private final ThreadPoolExecutor executor;

    private NsCacheLoader(@NotNull String name, @NotNull Function<K, V> key2valueLoader) {
      super();
      this.key2valueLoader = key2valueLoader;
      this.executor = createExecutor(name + "Cache", 1, 10, 1000);
    }

    @Override
    public V load(K key) {
      return key2valueLoader.apply(key);
    }

    @Override
    public ListenableFuture<V> reload(K key, V oldValue) {
      final ListenableFutureTask<V> task = ListenableFutureTask.create(() -> key2valueLoader.apply(key));
      executor.submit(task);
      return task;
    }

    @Override
    public Map<K, V> loadAll(Iterable<? extends K> keys) {
      return StreamSupport.stream(keys.spliterator(), false).collect(toMap(k -> k, key2valueLoader));
    }
  }

  public ThreadPoolExecutor createExecutor(
      @NotNull String name,
      int minThreadsNumber,
      int maxThreadsNumber,
      int queueSize
  ) {
    return new ThreadPoolExecutor(
        minThreadsNumber,
        maxThreadsNumber,
        1, MINUTES,
        new ArrayBlockingQueue<>(queueSize),
        new CountingThreadFactory(name),
        (runnable, executor) -> LOG.warn(name + " executor rejected " + runnable + " because of overflow (" + maxThreadsNumber + " threads, " + queueSize + " queue slots)")
    );
  }
}
