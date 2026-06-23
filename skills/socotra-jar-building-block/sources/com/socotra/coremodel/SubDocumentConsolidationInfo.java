package com.socotra.coremodel;

import com.socotra.platform.tools.ULID;
import lombok.Builder;

@Builder
public record SubDocumentConsolidationInfo(String name, ULID locator, int pages) {}
