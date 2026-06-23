package com.socotra.developer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.socotra.coremodel.Quote;
import java.util.Map;

/** Cannot be Java record since Jackson doesn't support @JsonUnwrapped for them */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class QuoteAPIResponse {
  @JsonUnwrapped private Quote quote;

  @JsonProperty("static")
  private Map<String, Object> staticData;

  public Quote quote() {
    return quote;
  }

  public Map<String, Object> staticData() {
    return staticData;
  }
}
