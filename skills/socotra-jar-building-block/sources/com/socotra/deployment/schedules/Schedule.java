package com.socotra.deployment.schedules;

import com.socotra.coremodel.CustomerObject;
import java.util.Iterator;
import java.util.stream.Stream;

/** The schedule interface provides a way to iterate over a schedule associated with an element. */
public interface Schedule extends AutoCloseable {
  <T extends CustomerObject> Iterator<T> iterator();

  <T extends CustomerObject> Stream<T> stream();
}
