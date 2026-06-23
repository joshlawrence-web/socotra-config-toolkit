package com.socotra.deployment.plugins;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.socotra.coremodel.*;
import com.socotra.coremodel.constraints.ValidationErrorResolver;
import com.socotra.deployment.DeploymentConfig;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Setter;

public class ConfigMigrationTransformer {
  private final ObjectMapper objectMapper;
  private final DeploymentFactory originalFactory;
  private final DeploymentFactory proxyFactory;
  private ValidationErrorResolver validationErrorResolver;
  private ProblemHandler problemHandler;

  public ConfigMigrationTransformer(DeploymentFactory factory) {
    this.objectMapper = factory.getObjectMapper().copy();
    // FAIL_ON_TRAILING_TOKENS prevents using JsonParser.readValueAs(...) while handling parsing
    // problems
    this.objectMapper.disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    this.originalFactory = factory;
    this.proxyFactory =
        new DeploymentFactory() {
          @Override
          public ObjectMapper getObjectMapper() {
            return objectMapper;
          }

          @Override
          public DeploymentConfig getDeploymentConfig() {
            return originalFactory.getDeploymentConfig();
          }

          @Override
          public ULID getStaticVersionLocator() {
            return originalFactory.getStaticVersionLocator();
          }
        };
  }

  public void deserializationErrorResolver(
      DeserializationErrorResolver deserializationErrorResolver) {
    if (problemHandler == null) {
      problemHandler = new ProblemHandler(deserializationErrorResolver);
      this.objectMapper.addHandler(problemHandler);
    } else {
      problemHandler.setDeserializationErrorResolver(deserializationErrorResolver);
    }
  }

  public void validationErrorResolver(ValidationErrorResolver validationErrorResolver) {
    this.validationErrorResolver = validationErrorResolver;
  }

  public <T extends Validatable<T> & CustomerObject> T transform(CustomerDataHolder object) {
    if (object == null) {
      return null;
    }
    return correct(object.toCustomerObject(proxyFactory));
  }

  public <T extends Validatable<T> & CustomerObject> T transform(Segment segment) {
    if (segment == null) {
      return null;
    }
    return correct(segment.toCustomerObject(proxyFactory));
  }

  public <T extends Validatable<T> & CustomerObject> T transform(Quote quote) {
    if (quote == null) {
      return null;
    }
    return correct(quote.toCustomerObject(proxyFactory));
  }

  public <T> T convert(Object object, Class<T> toClass) {
    return originalFactory.getObjectMapper().convertValue(object, toClass);
  }

  private <T extends Validatable<T> & CustomerObject> T correct(T object) {
    if (validationErrorResolver != null) {
      DeploymentConfig config = proxyFactory.getDeploymentConfig();
      T objectWithDefaults = object.applyDefaults(config);
      return objectWithDefaults.correct(config, validationErrorResolver);
    }
    return object;
  }

  private static class ProblemHandler extends DeserializationProblemHandler {
    @Setter private DeserializationErrorResolver deserializationErrorResolver;

    private ProblemHandler(DeserializationErrorResolver deserializationErrorResolver) {
      this.deserializationErrorResolver = deserializationErrorResolver;
    }

    @Override
    public boolean handleUnknownProperty(
        DeserializationContext ctxt,
        JsonParser parser,
        JsonDeserializer<?> deserializer,
        Object beanOrClass,
        String propertyName)
        throws IOException {
      if (deserializationErrorResolver == null) {
        return false;
      }
      Object value = readValue(parser);
      if (value != null) {
        deserializationErrorResolver.unknownProperty(beanOrClass, propertyName, value);
      }
      return true;
    }

    @Override
    public Object handleUnexpectedToken(
        DeserializationContext ctxt,
        JavaType targetType,
        JsonToken token,
        JsonParser parser,
        String failureMsg)
        throws IOException {
      if (deserializationErrorResolver == null) {
        return super.handleUnexpectedToken(ctxt, targetType, token, parser, failureMsg);
      }
      Object value = readValue(parser);
      if (value == null) {
        return null;
      }
      return deserializationErrorResolver.unexpectedValueType(
          null, targetType.getRawClass(), parser.currentName(), value);
    }

    private Object readValue(JsonParser parser) throws IOException {
      return switch (parser.currentToken()) {
        case START_OBJECT -> parser.readValueAs(Map.class);
        case START_ARRAY -> parser.readValueAs(List.class);
        case VALUE_FALSE, VALUE_TRUE -> parser.getBooleanValue();
        case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> parser.getNumberValue();
        case VALUE_NULL -> null;
        default -> parser.readValueAs(String.class);
      };
    }
  }
}
