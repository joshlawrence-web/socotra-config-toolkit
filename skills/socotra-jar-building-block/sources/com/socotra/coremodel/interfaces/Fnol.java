package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.*;
import com.socotra.coremodel.FnolLoss;
import com.socotra.coremodel.ValidationResult;
import com.socotra.deployment.DataFetcher;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Fnol<T> extends SensitiveDataHolder<T> {
  ULID locator();

  String type();

  FnolState fnolState();

  Optional<ULID> accountLocator();

  Optional<ULID> policyLocator();

  Optional<ULID> segmentLocator();

  Optional<Instant> incidentTime();

  Optional<String> incidentTimezone();

  Optional<String> incidentSummary();

  Optional<String> region();

  Optional<String> fnolNumber();

  default Collection<FnolLoss> losses() {
    return DataFetcher.getInstance().getFnolLosses(locator());
  }

  default Collection<ULID> claims() {
    return DataFetcher.getInstance().getFnolClaims(locator());
  }

  Collection<ContactRoles> contacts();

  Instant createdAt();

  UUID createdBy();

  default Optional<Instant> updatedAt() {
    return Optional.empty();
  }

  default Optional<UUID> updatedBy() {
    return Optional.empty();
  }

  default Optional<ValidationResult> validationResult() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }
}
