package com.socotra.coremodel;

import com.socotra.deployment.DeploymentConfig;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.util.*;

/** CustomerDataHolder holds customer's defined structures and can deserialize/serialize them. */
public interface CustomerDataHolder {
  ULID locator();

  Map<String, Object> data();

  String type();

  /**
   * Deserialize the data into customer's defined structure
   *
   * @param factory Customer deployment factory loaded from customer-config.jar
   * @return
   * @param <T>
   */
  @SuppressWarnings("unchecked")
  default <T> T resolve(DeploymentFactory factory) {
    return resolve(type(), factory);
  }

  /**
   * Deserialize the data into customer's defined structure
   *
   * @param type Customer object type name
   * @param factory Customer deployment factory loaded from customer-config.jar
   * @return
   * @param <T>
   */
  @SuppressWarnings("unchecked")
  default <T> T resolve(String type, DeploymentFactory factory) {
    DeploymentConfig config = factory.getDeploymentConfig();
    Class<? extends CustomerObject> clazz =
        config
            .getObjectClass(type)
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "There is no object class registered for type=" + type));
    return (T) factory.readObject(data(), clazz);
  }

  @SuppressWarnings("unchecked")
  default <T extends CustomerObject> T toCustomerObject(DeploymentFactory factory) {
    DeploymentConfig config = factory.getDeploymentConfig();
    Class<? extends CustomerObject> clazz =
        config
            .getObjectClass(type())
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "There is no object class registered for type=" + type()));
    return (T) factory.readObject(factory.getObjectMapper().convertValue(this, Map.class), clazz);
  }

  @SuppressWarnings("unchecked")
  default <T extends CustomerObject> T castData(DeploymentFactory factory, Class<T> clazz) {
    Object obj = resolve(factory);
    if (clazz.isInstance(obj)) {
      return (T) obj;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Update CustomerDataHolder object fields with values from Customer's datamodel representation
   *
   * @param factory
   * @param customerObject
   * @return
   * @param <T>
   */
  @SuppressWarnings("unchecked")
  default <T extends CustomerDataHolder> T mergeWith(
      DeploymentFactory factory, CustomerObject customerObject) {
    return (T) this;
  }

  /**
   * Serialize Customer's defined structure back to data object
   *
   * @param factory
   * @param objectDef
   * @return
   */
  static Map<String, Object> transform(DeploymentFactory factory, CustomerObject objectDef) {
    return factory.writeObject(objectDef);
  }

  /**
   * Patch data object will update/add corresponding key and values in destination data object.
   *
   * @param dst
   * @param patch
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static Map<String, Object> patchObject(Map<String, Object> dst, Map<String, Object> patch)
      throws UserInputException {
    if (patch == null || patch.isEmpty()) {
      return dst;
    }
    Map<String, Object> result = new HashMap<>(dst);
    ArrayList<String> errors = new ArrayList<>();
    for (Map.Entry<String, Object> entry : patch.entrySet()) {
      String entryKey = entry.getKey();
      Object entryValue = entry.getValue();
      Object srcV = result.get(entryKey);
      if (srcV == null || entryValue == null) {
        result.put(entryKey, entryValue);
      } else if (srcV instanceof Map m2 && entryValue instanceof Map m1) {
        try {
          result.put(entryKey, patchObject(m2, m1));
        } catch (UserInputException e) {
          errors.addAll(
              e.getErrors().stream().map(s -> String.format("%s.%s", entryKey, s)).toList());
        }
      } else if (srcV instanceof Map) {
        errors.add(
            entryKey
                + " is "
                + entryValue.getClass().getSimpleName()
                + " but the destination field is Map");
      } else if (entryValue instanceof Map) {
        errors.add(
            entryKey + " is Map but the destination field is " + srcV.getClass().getSimpleName());
      } else {
        result.put(entryKey, entryValue);
      }
    }

    if (!errors.isEmpty()) {
      throw new UserInputException("Data could not be patched", errors);
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Removes fields defined in `fields` data object.
   *
   * @param dst
   * @param fields
   * @return
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static Map<String, Object> removeFields(Map<String, Object> dst, Map<String, Object> fields) {
    if (fields == null || fields.isEmpty()) {
      return dst;
    }
    Map<String, Object> result = new HashMap<>(dst);
    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      if (entry.getValue() instanceof Map m1) {
        Object dstV = dst.get(entry.getKey());
        if (dstV == null) {
          continue;
        }
        if (dstV instanceof Map m2) {
          result.put(entry.getKey(), removeFields(m2, m1));
        } else {
          result.remove(entry.getKey());
        }
      } else {
        result.remove(entry.getKey());
      }
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Removes entries from the 'source' map that are specified in the 'toRemove' map, ignoring case
   * sensitivity of keys.
   *
   * <p>Example: Given source map: {"key1": "valA", "key2": {"nestedKey1": "nestedVal1"}} and
   * toRemove map: {"KEY1": "", "key2": {"NESTEDKEY1": ""}} The output will be: {"key2": {}}
   *
   * @param source The source map from which fields specified in the 'toRemove' map are to be
   *     removed.
   * @param toRemove The map containing keys that specify which fields should be removed from the
   *     'source' map. Keys in this map are treated case-insensitively.
   * @return A new map representing the 'source' map after removing the specified fields. The
   *     returned map is unmodifiable.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static Map<String, Object> removeFieldsIgnoreKeyCase(
      Map<String, Object> source, Map<String, Object> toRemove) {
    if (toRemove == null || toRemove.isEmpty()) {
      return source;
    }

    Map<String, Object> toRemoveCaseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    toRemoveCaseInsensitive.putAll(toRemove);

    Map<String, Object> result = new HashMap<>();

    for (String key : source.keySet()) {
      Object sourceValue = source.get(key);
      // keep original values if not found in "toRemove"
      if (!toRemoveCaseInsensitive.containsKey(key)) {
        result.put(key, sourceValue);
        continue;
      }
      // if original value already has a null entry, ignore toRemove fields, as there is nothing
      // to remove
      if (sourceValue == null) {
        result.put(key, null);
        continue;
      }
      // recursively search if values to source and to remove values are maps
      Object toRemoveValue = toRemoveCaseInsensitive.get(key);
      if (sourceValue instanceof Map sourceValueMap && toRemoveValue instanceof Map toRemoveMap) {
        result.put(key, removeFieldsIgnoreKeyCase(sourceValueMap, toRemoveMap));
      }
      // nothing left, at this point value is present in both source and target so it will not be
      // added to the end result
    }

    return Collections.unmodifiableMap(result);
  }

  /**
   * The function works similar to Spring StringUtils.capitalize/uncapitalize. But since we don't
   * want to have extra-dependencies for Plugins we cannot use the function from the library
   *
   * @param name
   * @return
   */
  private static String changeFirstCharacterCase(String name, boolean capitalize) {
    if (name == null || name.isBlank()) {
      return name;
    }
    char baseChar = name.charAt(0);
    char updatedChar;
    if (capitalize) {
      updatedChar = Character.toUpperCase(baseChar);
    } else {
      updatedChar = Character.toLowerCase(baseChar);
    }
    if (baseChar == updatedChar) {
      return name;
    }

    char[] chars = name.toCharArray();
    chars[0] = updatedChar;
    return new String(chars);
  }

  /**
   * Change name according to Data model naming convention
   *
   * @param name
   * @return
   */
  static String toDataModelName(String name) {
    return changeFirstCharacterCase(name, true);
  }

  /**
   * Change name according to Field naming convention
   *
   * @param name
   * @return
   */
  static String toDataFieldName(String name) {
    return changeFirstCharacterCase(name, false);
  }
}
