package com.solozobov.andrei.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * solozobov on 11/01/2019
 */
public abstract class Factory {

  @SafeVarargs
  public static <T> List<T> list(T ... elements) {
    return Arrays.asList(elements);
  }

  @SafeVarargs
  public static <T> Set<T> set(T ... elements) {
    return new HashSet<>(Arrays.asList(elements));
  }

  public static <A,B> List<B> map(Collection<A> collection, Function<A,B> mapper) {
    return collection.stream().map(mapper).collect(toList());
  }

  public static <A,B> List<B> map(Function<A,B> mapper, A ... elements) {
    return Stream.of(elements).map(mapper).collect(toList());
  }

  public static <K,V> Map<K,V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    final Map<K,V> result = new HashMap<>();
    result.put(k1, v1);
    result.put(k2, v2);
    result.put(k3, v3);
    result.put(k4, v4);
    return result;
  }

  public static <K,V> Map<K,V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
    final Map<K,V> result = new HashMap<>();
    result.put(k1, v1);
    result.put(k2, v2);
    result.put(k3, v3);
    result.put(k4, v4);
    result.put(k5, v5);
    result.put(k6, v6);
    result.put(k7, v7);
    result.put(k8, v8);
    result.put(k9, v9);
    return result;
  }

}
