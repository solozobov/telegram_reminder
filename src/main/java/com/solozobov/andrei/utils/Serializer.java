package com.solozobov.andrei.utils;

/**
 * solozobov on 07.07.2019
 */
public interface Serializer<DATA> {
  String serialize(DATA data);

  DATA deserialize(String string);
}
