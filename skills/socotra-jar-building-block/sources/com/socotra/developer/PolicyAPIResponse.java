package com.socotra.developer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.socotra.coremodel.Policy;
import java.util.Map;

/** Cannot be Java record since Jackson doesn't support @JsonUnwrapped for them */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PolicyAPIResponse {
  @JsonUnwrapped private Policy policy;

  @JsonProperty("static")
  Map<String, Object> staticData;

  public Policy policy() {
    return policy;
  }

  public Map<String, Object> staticData() {
    return staticData;
  }
}
