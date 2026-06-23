package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.SensitiveDataHolder;
import com.socotra.coremodel.views.Internal;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.UUID;

public interface ScheduleItem<T> extends SensitiveDataHolder<T> {
  String type();

  ULID locator();

  ULID staticElementLocator();

  @JsonView({Internal.class})
  ULID pageLocator();

  Instant createdAt();

  UUID createdBy();
}
