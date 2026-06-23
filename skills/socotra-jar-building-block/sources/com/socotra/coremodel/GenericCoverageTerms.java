package com.socotra.coremodel;

public interface GenericCoverageTerms<T> extends CustomerObject {
  CoverageTermsType coverageType();

  boolean isDefault();

  String displayName();

  T value();
}
