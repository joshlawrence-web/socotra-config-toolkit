package com.socotra.coremodel;

import lombok.Builder;

@Builder
public record AutomationActionDetails(
    String methodName, boolean takesRequest, boolean returnsResponse, int timeout) {}
