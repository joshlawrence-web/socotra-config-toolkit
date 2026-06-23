package com.socotra.coremodel;

import java.util.Collection;
import lombok.Builder;

@Builder
public record DocumentConsolidationInfo(
    String name, Collection<SubDocumentConsolidationInfo> subDocuments) {}
