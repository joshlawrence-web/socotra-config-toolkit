package com.socotra.deployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.socotra.platform.tools.ULID;
import java.util.function.Supplier;

public abstract class AbstractDeploymentFactory implements DeploymentFactory {

  private static Supplier<ObjectMapper> defaultObjectMapperSupplier;

  private final ObjectMapper objectMapper =
      JsonMapper.builder()
          .serializationInclusion(JsonInclude.Include.NON_EMPTY)
          .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
          .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
          .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
          .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
          .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
          .disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .findAndAddModules()
          .build();

  @Override
  public ULID getStaticVersionLocator() {
    return null;
  }

  @Override
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * The helper method to use JSON serialization/deserialization with plugin code. Unpropper usage
   * of the method within backend services will cause JVM non-heap memory leak (see:
   * ObjectMapper._rootDeserializers and <a
   * href="https://socotra.atlassian.net/browse/KERN-3014">KERN-3014</a>)
   *
   * @return default Object mapper instance
   */
  public static ObjectMapper defaultMapper() {
    if (defaultObjectMapperSupplier == null) {
      throw new IllegalStateException("The method should be used in plugins only");
    }
    return defaultObjectMapperSupplier.get();
  }

  public static void setDefaultObjectMapperSupplier(Supplier<ObjectMapper> supplier) {
    defaultObjectMapperSupplier = supplier;
  }
}
