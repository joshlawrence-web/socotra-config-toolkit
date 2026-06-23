package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.socotra.coremodel.ValidationItem;
import com.socotra.coremodel.ValidationResult;
import java.util.regex.Pattern;

/**
 * Represents a category of Events for use as a filter in the Events Stream API. <br>
 * The Events Service does not manage a list of EventTypes. It only validates that EventTypes match
 * the required regex when creating new Events. <br>
 * Each internal service that wants to publish Events is responsible for providing their own
 * EventTypes, which can be done as a class, enum, or record which implements this interface.
 */
public interface EventType {

  /**
   * Regex pattern used to validate EventTypes. <br>
   * Translation: Lowercase alphanumeric with period delimiters between 1 and 126 characters.
   */
  Pattern REGEX = Pattern.compile("^[a-zA-Z0-9.]{1,127}$");

  String ERROR_FORMAT =
      "Invalid event type '%s'. Must be between 1 and 126 characters "
          + "and can only contain letters, numbers, and/or periods (must match regex: '%s').";

  /**
   * Validates that the EventType id matches the regex.
   *
   * @param eventType the EventType to validate
   * @return ValidationResult the result of the validation.
   */
  static ValidationResult validate(EventType eventType) {
    return validate(eventType == null ? null : eventType.id());
  }

  /**
   * Validates that the EventType id matches the regex.
   *
   * @param id the id of the EventType to validate
   * @return ValidationResult the result of the validation.
   */
  static ValidationResult validate(String id) {
    ValidationResult.ValidationResultBuilder results = ValidationResult.builder();
    if (id == null || !REGEX.matcher(id).matches()) {
      results.addValidationItem(
          ValidationItem.builder()
              .elementType("event.type")
              .addError(ERROR_FORMAT.formatted(id, REGEX))
              .build());
    }
    return results.build();
  }

  /**
   * Convenience implementation for creating simple EventTypes.
   *
   * @param id the string representation of the EventType.
   */
  @JsonCreator
  static EventType of(String id) {
    return new Simple(id);
  }

  /**
   * String representation of the EventType that is used for serialization, persistence, rest/gRPC
   * APIs, etc. Must conform to the regex pattern in order to pass validation when creating Events.
   *
   * @return the string representation of this EventType.
   */
  @JsonValue
  String id();

  /**
   * Returns a payload record class associated with the event
   *
   * @return
   */
  default Class<? extends Record> payloadClass() {
    return null;
  }

  /**
   * Convenience implementation for creating simple EventTypes.
   *
   * @param id the string representation of the EventType.
   */
  record Simple(String id) implements EventType {}
}
