package com.socotra.coremodel;

public enum ComplianceReferenceType {
  none, // used for indicating that anonymization reference has no parent reference type
  account,
  quickQuote,
  quote,
  policy,
  contact,
  fnol,
  payment,
  disbursement,
}
