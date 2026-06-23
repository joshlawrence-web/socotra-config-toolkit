package com.socotra.coremodel;

import java.util.Collection;
import java.util.Optional;

public record ContactSlot(Collection<String> types, Integer minSize, Optional<Integer> maxSize) {
  public static ContactSlot of(Collection<String> types, Integer minSize) {
    return new ContactSlot(types, minSize, Optional.empty());
  }

  public static ContactSlot of(Collection<String> types, Integer minSize, Integer maxSize) {
    return new ContactSlot(types, minSize, Optional.ofNullable(maxSize));
  }
}
