package com.solozobov.andrei.utils;

import java.util.function.Function;

/**
 * solozobov on 05.11.2019
 */
public interface Converter<FROM,TO> {

  TO convert(FROM a);

  FROM reverse(TO to);

  static <FROM, TO> Converter<FROM, TO> create(Function<FROM, TO> forward, Function<TO, FROM> backward) {
    return new Converter<FROM, TO>() {
      @Override
      public TO convert(FROM from) {
        return forward.apply(from);
      }

      @Override
      public FROM reverse(TO to) {
        return backward.apply(to);
      }
    };
  }
}
