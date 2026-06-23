package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.*;
import java.util.Collection;
import java.util.Optional;

public interface DocumentConfig {
  String name();

  DocumentScope scope();

  DocumentFormat format();

  DocumentRendering rendering();

  SelectionTimeBasis selectionTimeBasis();

  DocumentTrigger trigger();

  Collection<String> templateSnippets();

  Collection<String> customFonts();

  /*-
   * Slowly migrating from ResourceTypes to ResourceType
   *
   * <p>1. TODO: On 10/01/2024 change resourceType() to return ResourceType
   *
   * <p>2. TODO: On 10/01/2024 generate com.socotra.deployment.customer.DocumentConfigs to use
   * resourceType()
   *
   * @return
   */
  default ResourceTypes resourceType() {
    return rendering() == DocumentRendering.dynamic
        ? ResourceTypes.documentTemplate
        : ResourceTypes.staticDocument;
  }

  /*-
   * Slowly migrating from ResourceTypes to ResourceType
   *
   * <p>1. TODO: Generate com.socotra.deployment.customer.DocumentConfigs to use resourceTypes()
   *
   * <p>2. TODO: On 1/1/2025 remove resourceTypes()
   *
   * @return
   */
  default ResourceTypes resourceTypes() {
    return rendering() == DocumentRendering.dynamic
        ? ResourceTypes.documentTemplate
        : ResourceTypes.staticDocument;
  }

  Optional<Boolean> portrait();

  Optional<DocumentSize> pageSize();

  Optional<com.socotra.coremodel.DocumentMargin> margin();

  default Optional<String> displayName() {
    return Optional.empty();
  }
}
