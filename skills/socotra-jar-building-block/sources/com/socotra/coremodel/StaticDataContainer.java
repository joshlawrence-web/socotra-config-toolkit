package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.socotra.coremodel.interfaces.Quote;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class StaticDataContainer implements CustomerDataHolder {
  public static final String QUOTE_STATIC_DATA = "QuoteStaticData";
  public static final String POLICY_STATIC_DATA = "PolicyStaticData";
  private final ULID locator;
  private final Map<String, Object> data;
  private final String type;

  private StaticDataContainer(String type, ULID locator, Map<String, Object> data) {
    this.type = CustomerDataHolder.toDataModelName(type);
    this.locator = locator;
    this.data = data;
  }

  @Override
  public ULID locator() {
    return locator;
  }

  @Override
  public Map<String, Object> data() {
    return data;
  }

  @Override
  public String type() {
    return type;
  }

  @Deprecated
  public Map<String, Object> toMap(DeploymentFactory factory) throws ValidationException {
    Instant now = Instant.now();
    return toMap(factory, new Validatable.ValidatableContext(now, now));
  }

  public Map<String, Object> toMap(DeploymentFactory factory, Validatable.ValidatableContext ctx)
      throws ValidationException {
    Validatable v = resolve(factory);
    ValidationResult result =
        ValidationResult.builder()
            .validationItems(v.validate(factory.getDeploymentConfig(), ctx))
            .build();
    if (!result.success()) {
      throw new ValidationException("Failed to validate the static data", result);
    }
    return CustomerDataHolder.transform(factory, (CustomerObject) v);
  }

  @Override
  @SuppressWarnings("unchecked")
  public StaticDataContainer mergeWith(DeploymentFactory factory, CustomerObject customerObject) {
    if (customerObject instanceof SensitiveObject) {
      return new StaticDataContainer(
          type(), locator(), CustomerDataHolder.transform(factory, customerObject));
    }
    return this;
  }

  public static final class QuoteStaticDataContainer extends StaticDataContainer {

    @JsonCreator
    private QuoteStaticDataContainer(
        @JsonProperty("type") String type,
        @JsonProperty("locator") ULID locator,
        @JsonProperty("data") Map<String, Object> data) {
      super(type, locator, data);
    }

    public QuoteStaticDataContainer(Quote quote, Map<String, Object> data) {
      super(quote.productName() + QUOTE_STATIC_DATA, quote.locator(), data);
    }
  }

  public static final class PolicyStaticDataContainer extends StaticDataContainer {
    @JsonCreator
    private PolicyStaticDataContainer(
        @JsonProperty("type") String type,
        @JsonProperty("locator") ULID locator,
        @JsonProperty("data") Map<String, Object> data) {
      super(type, locator, data);
    }

    public PolicyStaticDataContainer(Policy policy, Map<String, Object> data) {
      super(policy.productName() + POLICY_STATIC_DATA, policy.locator(), data);
    }
  }
}
