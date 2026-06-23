package com.socotra.deployment;

import java.util.Iterator;
import java.util.stream.Stream;

public interface StreamingEntity<T> extends AutoCloseable {
  Iterator<T> iterator();

  Stream<T> stream();
}
