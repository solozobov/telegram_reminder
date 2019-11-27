package com.solozobov.andrei.utils;

import java.util.Objects;

/**
 * solozobov on 11/01/2019
 */
public class Pair<F,S> {
  public final F first;
  public final S second;

  public static <F,S> Pair<F, S> of(F first, S second) {
    return new Pair<>(first, second);
  }

  private Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  public F first() {
    return first;
  }

  public S second() {
    return second;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (!(o instanceof Pair)) { return false; }
    final Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "{" + first + "," + second + "}";
  }
}
