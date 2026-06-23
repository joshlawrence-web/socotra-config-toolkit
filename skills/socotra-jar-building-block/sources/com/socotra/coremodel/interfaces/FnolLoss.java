package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.FnolLossState;
import com.socotra.coremodel.SensitiveDataHolder;
import com.socotra.coremodel.ValidationResult;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface FnolLoss<T> extends SensitiveDataHolder<T> {
  ULID locator();

  String type();

  String category();

  FnolLossState fnolLossState();

  Optional<ULID> exposureElementLocator();

  Optional<ULID> coverageElementLocator();

  default Collection<String> coverageTypes() {
    return null;
  }

  default Optional<ValidationResult> validationResult() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }
}
