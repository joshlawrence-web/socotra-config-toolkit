package com.socotra.coremodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** TODO: Backward compatibility class. Remove after 11/04/2024 */
@Deprecated
public abstract class DataSecurity {
  protected Map<String, Object> pathToDataSecurityFieldType = new HashMap<>();
  protected Map<String, Object> securityFields = new HashMap<>();
  protected Map<String, String> indexToOption = new HashMap<>();

  protected DataSecurity(
      List<String> accountFields, List<String> policyFields, String defaultRegion) {}
}
