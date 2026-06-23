package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import com.socotra.platform.tools.ULID;

@Plugin(type = PluginType.autopay)
public interface AutopayPlugin {
  default AutopayPluginResponse autopay(AutopayRequest request) {
    return AutopayPluginResponse.builder().build();
  }

  public static final class AutopayPluginStub implements AutopayPlugin {
  }

  public static record AutopayRequest(Invoice invoice) implements PluginRequest {
    public Collection<?> structures() { return List.of(invoice()); }
    public static AutopayPluginRequest of(Invoice invoice) {
      Objects.requireNonNull(invoice);
      return new AutopayPluginRequest(invoice);
    }
  }
}