package com.socotra.coremodel;

import java.util.*;

public abstract class DataAccessControl {
  public static final String PRODUCT_PREFIX = "productName.";
  public static final String REGION_PREFIX = "region.";
  public static final String ACCOUNT_PREFIX = "account.";
  public static final String POLICY_PREFIX = "policy.";
  protected Map<String, DataAccessControlField.FieldType> pathToDataAccessControlFieldType =
      new HashMap<>();
  protected Map<String, DataAccessControlField> dataAccessControlFields = new HashMap<>();
  protected Map<String, String> indexToOption = new HashMap<>();
  private final ArrayList<String> accountFields;
  private final ArrayList<String> policyFields;
  private final String defaultRegion;

  protected DataAccessControl(
      List<String> accountFields, List<String> policyFields, String defaultRegion) {
    this.accountFields = new ArrayList<>(accountFields);
    this.policyFields = new ArrayList<>(policyFields);
    this.defaultRegion = defaultRegion;
  }

  /**
   * Return if dataAccess control is enabled
   *
   * @return
   */
  public abstract boolean isEnabled();

  /**
   * Return if data masking is enabled
   *
   * @return
   */
  public boolean isDataMaskingEnabled() {
    return false;
  }

  /**
   * Calculates securityId for an account
   *
   * @param account
   * @return
   */
  public BitSet calculate(Account account) {
    BitSet result = new BitSet();
    for (int i = 0; i < accountFields.size(); i++) {
      int offset = i * DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
      securityField(ACCOUNT_PREFIX + accountFields.get(i), account)
          .ifPresent(f -> result.set(f.index() + offset));
    }
    return result;
  }

  /**
   * calculates securityId for quote
   *
   * @param quote
   * @return
   */
  public BitSet calculate(com.socotra.coremodel.interfaces.Quote quote) {
    BitSet result = new BitSet();
    for (int i = 0; i < policyFields.size(); i++) {
      int offset = i * DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
      securityField(POLICY_PREFIX + policyFields.get(i), quote)
          .ifPresent(f -> result.set(f.index() + offset));
    }
    return result;
  }

  /**
   * Calculates securityId for policy and transaction's post-split segment
   *
   * @param policy
   * @param postSplitSegment
   * @return
   */
  public BitSet calculate(Policy policy, Segment postSplitSegment) {
    BitSet result = new BitSet();
    for (int i = 0; i < policyFields.size(); i++) {
      int offset = i * DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
      securityField(POLICY_PREFIX + policyFields.get(i), policy, postSplitSegment)
          .ifPresent(f -> result.set(f.index() + offset));
    }
    return result;
  }

  public Map<String, DataAccessControlField> securityFields() {
    return Collections.unmodifiableMap(dataAccessControlFields);
  }

  public DataAccessControlField.FieldType securityFieldType(String path) {
    return pathToDataAccessControlFieldType.get(path);
  }

  private BitSet calculateMask(
      List<String> fields, Map<String, Collection<String>> fieldValues, String prefix) {
    BitSet result = new BitSet();
    Map<String, Collection<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    map.putAll(fieldValues);
    for (int i = 0; i < fields.size(); i++) {
      Collection<String> values = map.get(fields.get(i));
      if (values == null) {
        continue;
      }
      DataAccessControlField.FieldType type = securityFieldType(prefix + fields.get(i));
      if (type == null) {
        continue;
      }

      int offset = i * DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
      for (String value : values) {
        if (value.equals("*")) {
          result.set(offset, offset + DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET - 1);
        } else {
          DataAccessControlField field =
              switch (type) {
                case product -> dataAccessControlFields.get(PRODUCT_PREFIX + value);
                case region -> dataAccessControlFields.get(REGION_PREFIX + value);
                case dataField ->
                    dataAccessControlFields.get(prefix + value + "-" + prefix + fields.get(i));
              };
          if (field != null) {
            result.set(field.index() + offset);
          }
        }
      }
    }
    return result;
  }

  /**
   * Calculate user's mask to allow access. Fields provides a configured security field and
   * corresponding values. `*` means all values for given field
   *
   * @param fields
   * @return
   */
  public BitSet calculateAccountMask(Map<String, Collection<String>> fields) {
    return calculateMask(accountFields, fields, ACCOUNT_PREFIX);
  }

  /**
   * Calculate user's mask to allow access. Fields provides a configured security field and
   * corresponding values. `*` means all values for given field
   *
   * @param fields
   * @return
   */
  public BitSet calculatePolicyMask(Map<String, Collection<String>> fields) {
    return calculateMask(policyFields, fields, POLICY_PREFIX);
  }

  /**
   * Syntax sugar method to calculate User's mask
   *
   * @param type
   * @param fields
   * @return
   */
  public BitSet calculateMask(DataAccessControlType type, Map<String, Collection<String>> fields) {
    return switch (type) {
      case account -> calculateAccountMask(fields);
      case policy -> calculatePolicyMask(fields);
    };
  }

  /**
   * Converts User mask back to field values representation
   *
   * @param type
   * @param mask
   * @return
   */
  public Map<String, Collection<String>> maskToFieldValues(
      DataAccessControlType type, BitSet mask) {
    Map<String, Collection<String>> result = new HashMap<>();
    ArrayList<String> fields =
        switch (type) {
          case account -> accountFields;
          case policy -> policyFields;
        };
    String prefix =
        switch (type) {
          case account -> ACCOUNT_PREFIX;
          case policy -> POLICY_PREFIX;
        };
    for (int i = 0; i < fields.size(); i++) {
      String field = fields.get(i);
      Collection<String> values = new ArrayList<>();
      int offset = i * DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
      BitSet star = new BitSet();
      star.set(offset, offset + DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET - 1);
      BitSet tmp = BitSet.valueOf(star.toByteArray());
      tmp.and(mask);
      if (star.equals(tmp)) {
        values.add("*");
      } else {
        for (int y = offset;
            y < offset + DataAccessControlField.DATA_SECURITY_FIELD_INDEX_OFFSET;
            y++) {
          if (mask.get(y)) {
            if (field.startsWith("data")) {
              values.add(indexToOption.get(prefix + field + (y - offset)));
            } else {
              values.add(indexToOption.get(field + (y - offset)));
            }
          }
        }
      }
      result.put(fields.get(i), values);
    }
    return result;
  }

  /**
   * Returns configured DataSecurityField for given path and account. The path is resolved into
   * DataSecurityFieldType
   *
   * @param path
   * @param account
   * @return
   */
  public Optional<DataAccessControlField> securityField(String path, Account account) {
    DataAccessControlField.FieldType type = securityFieldType(path);
    if (type == null) {
      return Optional.empty();
    }
    return switch (type) {
      case product -> Optional.empty();
      case region ->
          account
              .region()
              .map(r -> dataAccessControlFields.get(REGION_PREFIX + r))
              .or(
                  () ->
                      Optional.ofNullable(
                          dataAccessControlFields.get(REGION_PREFIX + defaultRegion)));
      case dataField ->
          DataAccessControl.getDataValue(path.replaceFirst("account\\.", ""), account.data())
              .map(v -> securityFields().get(ACCOUNT_PREFIX + v + "-" + path));
    };
  }

  public Optional<DataAccessControlField> securityField(
      String path, com.socotra.coremodel.interfaces.Quote quote) {
    DataAccessControlField.FieldType type = securityFieldType(path);
    if (type == null) {
      return Optional.empty();
    }
    return switch (type) {
      case product ->
          Optional.ofNullable(dataAccessControlFields.get(PRODUCT_PREFIX + quote.productName()));
      case region ->
          quote
              .region()
              .map(r -> dataAccessControlFields.get(REGION_PREFIX + r))
              .or(
                  () ->
                      Optional.ofNullable(
                          dataAccessControlFields.get(REGION_PREFIX + defaultRegion)));
      case dataField ->
          DataAccessControl.getDataValue(path.replaceFirst("policy\\.", ""), quote.element().data())
              .map(v -> dataAccessControlFields.get(POLICY_PREFIX + v + "-" + path));
    };
  }

  /**
   * Returns configured DataSecurityField for given path, policy and postSplitSegment. The path is
   * resolved into DataSecurityFieldType
   *
   * @param path
   * @param policy
   * @param postSplitSegment
   * @return
   */
  public Optional<DataAccessControlField> securityField(
      String path, Policy policy, Segment postSplitSegment) {
    DataAccessControlField.FieldType type = securityFieldType(path);
    if (type == null) {
      return Optional.empty();
    }
    return switch (type) {
      case product ->
          Optional.ofNullable(securityFields().get(PRODUCT_PREFIX + policy.productName()));
      case region -> policy.region().map(r -> dataAccessControlFields.get(REGION_PREFIX + r));
      case dataField ->
          DataAccessControl.getDataValue(
                  path.replaceFirst("policy\\.", ""), postSplitSegment.element().data())
              .map(v -> securityFields().get(POLICY_PREFIX + v + "-" + path));
    };
  }

  /**
   * Check if securityId matches to mask.
   *
   * @param securityId
   * @param mask
   * @return
   */
  public boolean isAllowed(BitSet securityId, long[] mask) {
    BitSet msk = BitSet.valueOf(mask);
    msk.and(securityId);
    return msk.equals(securityId);
  }

  /**
   * Returns a value in String representation for given path within map. The path has to begin with
   * `data.`
   *
   * @param path
   * @param data
   * @return
   */
  @SuppressWarnings("unchecked")
  static Optional<String> getDataValue(String path, Map<String, Object> data) {
    String[] parts = path.split("\\.");
    if (parts.length < 2) {
      throw new IllegalArgumentException("path should start with `data.` and specify field name");
    }
    Object val = null;
    for (int i = 1; i < parts.length; i++) {
      val = data.get(CustomerDataHolder.toDataFieldName(parts[i]));
      if (val == null) {
        return Optional.empty();
      }
      if (val instanceof Map<?, ?> mp) {
        data = (Map<String, Object>) mp;
      } else {
        break;
      }
    }
    return Optional.of(val.toString());
  }

  /**
   * Format the key to lookup DataSecurityField
   *
   * @param prefix
   * @param field
   * @param value
   * @return
   */
  public static String formatOptionValue(String prefix, String field, String value) {
    if (field.startsWith("data.")) {
      return prefix + "." + value + "-" + prefix + "." + normalizeDataFieldPath(field);
    }

    return field + "." + value;
  }

  /**
   * Normalize the path to make sure the field names are in proper naming convention
   *
   * @param path
   * @return
   */
  public static String normalizeDataFieldPath(String path) {
    if (!path.startsWith("data")) {
      return CustomerDataHolder.toDataFieldName(path);
    }
    String[] parts = path.split("\\.");
    if (parts.length > 1) {
      for (int i = 1; i < parts.length; i++) {
        parts[i] = CustomerDataHolder.toDataFieldName(parts[i]);
      }
    }
    return String.join(".", parts);
  }
}
