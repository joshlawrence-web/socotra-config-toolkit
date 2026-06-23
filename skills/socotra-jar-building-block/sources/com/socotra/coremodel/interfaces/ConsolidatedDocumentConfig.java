package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.ConsolidatedPageNumbering;
import com.socotra.coremodel.DocumentTrigger;
import java.util.Collection;
import java.util.Optional;

public interface ConsolidatedDocumentConfig {
  String name();

  Collection<String> consolidatedDocuments();

  default Optional<String> leadingDocumentTemplate() {
    return Optional.empty();
  }

  default Optional<ConsolidatedPageNumbering> pageNumbering() {
    return Optional.empty();
  }

  DocumentTrigger trigger();
}
