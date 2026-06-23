package com.socotra.coremodel;

/** TODO: Backward compatibility class. Remove after 11/04/2024 */
@Deprecated
public record DataSecurityField(DataSecurityFieldType type, String path, int index) {
  public static int DATA_SECURITY_FIELD_INDEX_OFFSET = 256;

  public enum DataSecurityFieldType {
    product,
    region,
    dataField
  }
}
