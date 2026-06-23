package com.socotra.coremodel;

import lombok.Builder;

@Builder
public record MoratoriumRule(String path, String criteriaKey, boolean notIn) {}
