package com.socotra.coremodel;

public record DataAccessControlField(FieldType type, String path, int index) {
  public static int DATA_SECURITY_FIELD_INDEX_OFFSET = 256;

  public enum FieldType {
    product,
    region,
    dataField
  }
}
