package com.socotra.deployment.plugins;

public interface DeserializationErrorResolver {
  default <T, V> V unexpectedValueType(
      Class<T> targetObjectType, Class<V> expectedType, String propertyName, Object value) {
    return null;
  }

  default void unknownProperty(Object beanOrClass, String propertyName, Object propertyValue) {}
}
