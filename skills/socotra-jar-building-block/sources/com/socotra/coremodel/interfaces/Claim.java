package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.ClaimState;
import com.socotra.coremodel.SensitiveDataHolder;
import com.socotra.coremodel.ValidationResult;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Claim<T> extends SensitiveDataHolder<T> {
  String type();

  ULID locator();

  ClaimState claimState();

  Optional<ULID> fnolLocator();

  Instant createdAt();

  UUID createdBy();

  Optional<ValidationResult> validationResult();
}
