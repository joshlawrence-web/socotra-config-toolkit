package com.socotra.coremodel.constraints;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ValidationErrorResolver {
  public final <T extends Record, V> void correct(
      T record, RecordComponent field, Consumer<V> resolution, StaticConstraint<V>... constraints) {
    if (constraints == null || constraints.length == 0) {
      return;
    }
    try {
      Method accessor = field.getAccessor();
      V value = (V) accessor.invoke(record);

      correctedValue(record, field, value, Arrays.asList(constraints))
          .ifPresentOrElse(resolution, () -> resolution.accept(null));
    } catch (Exception e) {
      log.warn(
          "Failed to correct value for {}.{}",
          field.getDeclaringRecord().getName(),
          field.getName(),
          e);
    }
  }

  public final <T extends Record, V> Optional<V> correctedValue(
      T record,
      RecordComponent field,
      V currentValue,
      Collection<StaticConstraint<V>> constraints) {
    if (constraints == null) {
      return Optional.ofNullable(currentValue);
    }
    Collection<StaticConstraint<V>> violatedConstraints =
        constraints.stream().filter(c -> !c.isValid(currentValue)).toList();

    if (violatedConstraints.isEmpty()) {
      return Optional.ofNullable(currentValue);
    }

    return correctedValueImpl(record, field, currentValue, violatedConstraints);
  }

  /**
   * Gets called on either scalar field value or each collection item of a collection field if the
   * value does not satisfy the constraint.
   *
   * @param record the record containing the field
   * @param field
   * @param value value of the field if the field is a scalar or a value of a collection item if the
   *     field is a collection
   * @param violatedConstraints the constraints that the value does not satisfy
   * @return the corrected value or empty if the value should be discarded. Return the input value
   *     if it should be kept as is.
   * @param <T> the type of the record
   * @param <V> the type of the field or collection item
   */
  public <T extends Record, V> Optional<V> correctedValueImpl(
      T record,
      RecordComponent field,
      V value,
      Collection<StaticConstraint<V>> violatedConstraints) {
    if (violatedConstraints == null) {
      return Optional.ofNullable(value);
    }
    try {
      for (StaticConstraint<V> constraint : violatedConstraints) {
        value = constraint.correct(value);
      }
    } catch (Exception ignored) {
    }
    return Optional.ofNullable(value);
  }
}
