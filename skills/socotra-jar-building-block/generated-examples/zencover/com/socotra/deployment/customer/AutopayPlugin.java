package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import com.socotra.platform.tools.ULID;

@Plugin(type = PluginType.autopay)
public interface AutopayPlugin {
  public static final Integer VERSION = 1;
  default AutopayPluginResponse autopay(AutopayRequest request) {
    return AutopayPluginResponse.builder().build();
  }

  public static final class AutopayPluginStub implements AutopayPlugin {
  }

  public static record AutopayRequest(Invoice invoice, Boolean invoicingHoldActive) implements PluginRequest {
    public Collection<?> structures() { return List.of(invoice(), invoicingHoldActive()); }
    public static AutopayPluginRequest of(Invoice invoice, Boolean invoicingHoldActive) {
      Objects.requireNonNull(invoice);
      return AutopayPluginRequest.builder().invoice(invoice).invoicingHoldActive(invoicingHoldActive).build();
    }
  }
}