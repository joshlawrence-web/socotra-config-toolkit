package com.socotra.deployment;

import com.socotra.coremodel.CustomerObject;
import com.socotra.coremodel.RangeTableMetadata;
import com.socotra.platform.tools.Interpolation;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

public interface RangeTableRecordFetcher<T extends RangeTableMetadata & CustomerObject> {
  Optional<T> getRecord(byte[] key, BigDecimal boundValue);

  /**
   * Get the record for the given key and range selection start that is closest to but not greater
   * than the given boundValue. If multiple ranges match, the one with the lowest range end is
   * returned.
   *
   * @param key
   * @param boundValue
   * @return
   */
  Optional<T> getLowerAdjacentRecord(byte[] key, BigDecimal boundValue);

  /**
   * Get the record for the given key and range selection start that is closest to but not less than
   * the given boundValue. If multiple ranges match, the one with the lowest range end is returned.
   *
   * @param key
   * @param boundValue
   * @return
   */
  Optional<T> getUpperAdjacentRecord(byte[] key, BigDecimal boundValue);

  default Optional<BigDecimal> interpolate(
      byte[] key,
      BigDecimal boundValue,
      Function<T, ? extends Number> mapper,
      Interpolation method) {
    Optional<T> upper = getUpperAdjacentRecord(key, boundValue);
    Optional<T> lower = getLowerAdjacentRecord(key, boundValue);
    if (upper.isEmpty() || lower.isEmpty()) {
      return Optional.empty();
    }
    BigDecimal x1 = lower.map(T::rangeStart).orElseThrow();
    BigDecimal x2 = upper.map(T::rangeStart).orElseThrow();
    BigDecimal y1 = lower.map(mapper).map(Object::toString).map(BigDecimal::new).orElseThrow();
    BigDecimal y2 = upper.map(mapper).map(Object::toString).map(BigDecimal::new).orElseThrow();
    return Optional.of(method.apply(x1, y1, x2, y2, boundValue));
  }

  default Optional<BigDecimal> extrapolate(
      byte[] key,
      BigDecimal boundValue,
      Function<T, ? extends Number> mapper,
      Interpolation method) {
    Optional<T> upper = getUpperAdjacentRecord(key, boundValue);
    Optional<T> lower = getLowerAdjacentRecord(key, boundValue);
    if (upper.isEmpty() && lower.isEmpty()) {
      return Optional.empty();
    }
    if (upper.isEmpty()) {
      upper = lower;
      lower =
          upper.flatMap(
              v ->
                  getLowerAdjacentRecord(
                      key, v.rangeStart().subtract(BigDecimal.ONE.movePointLeft(8))));
      lower = lower.isPresent() ? lower : upper;
    } else if (lower.isEmpty()) {
      lower = upper;
      upper =
          lower.flatMap(
              v ->
                  getUpperAdjacentRecord(key, v.rangeStart().add(BigDecimal.ONE.movePointLeft(8))));
      upper = upper.isPresent() ? upper : lower;
    }
    BigDecimal x1 = lower.map(T::rangeStart).orElseThrow();
    BigDecimal x2 = upper.map(T::rangeStart).orElseThrow();
    BigDecimal y1 = lower.map(mapper).map(Object::toString).map(BigDecimal::new).orElseThrow();
    BigDecimal y2 = upper.map(mapper).map(Object::toString).map(BigDecimal::new).orElseThrow();
    return Optional.of(method.apply(x1, y1, x2, y2, boundValue));
  }
}
