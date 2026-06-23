package com.socotra.platform.tools;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableUtils {
  // Do not change the marker!
  private static final String COLUMN_KEY_MARKER = "F713X5YLZK";

  public static byte[] makeKey(String... values) {
    return makeKey(() -> Arrays.stream(values).iterator());
  }

  public static byte[] makeColumnKey(String column) {
    return makeKey(COLUMN_KEY_MARKER, column);
  }

  public static byte[] makeKey(Iterable<String> values) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      log.error("Failed to create digest", e);
      throw new RuntimeException("Failed to create digest", e);
    }
    String flatKey = String.join(":", values).toUpperCase();
    digest.update(flatKey.getBytes());
    return digest.digest();
  }

  public static void validateKey(Object... parts) {
    int setCount = 0, nullCount = 0;
    for (int i = 0; i < parts.length && parts[i] != null; ++i) {
      ++setCount;
    }
    for (int i = setCount; i < parts.length && parts[i] == null; ++i) {
      ++nullCount;
    }
    if (setCount + nullCount < parts.length) {
      throw new IllegalArgumentException("Incorrect arrangement of set and unset attributes");
    }
  }

  public static byte[] makePartialKey(Object... parts) {
    validateKey(parts);
    return makeKey(
        Arrays.stream(parts).takeWhile(Objects::nonNull).map(TableUtils::toKeyString).toList());
  }

  public static String toKeyString(Object obj) {
    if (obj instanceof BigDecimal decimal) {
      return NumberUtils.trimScale(decimal).toString();
    }
    return obj.toString();
  }
}
