package com.socotra.coremodel;

import com.socotra.coremodel.constraints.ValidationErrorResolver;
import com.socotra.deployment.DeploymentConfig;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Validatable<T extends Validatable<?>> {
  @Deprecated(since = "2024-12-04")
  default Optional<Collection<ValidationItem>> validate(DeploymentConfig config) {
    throw new UnsupportedOperationException(
        "Deprecated in favor of `validate(DeploymentConfig config, ValidatableContext context)`");
  }

  default Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    return validate(config).orElse(List.of());
  }

  @SuppressWarnings("unchecked")
  default T correct(DeploymentConfig config, ValidationErrorResolver resolver) {
    return (T) this;
  }

  record ValidatableContext(Instant originalReferenceTime, Instant currentReferenceTime) {}
}
