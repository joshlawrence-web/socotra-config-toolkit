package com.socotra.deployment.customer;

import com.socotra.coremodel.*;

import java.util.*;
import java.util.List;

@Plugin(type = PluginType.paymentPostProcessing)
public interface PaymentPostProcessingPlugin {

  public static final Integer VERSION = 1;

  default PaymentPostProcessingResponse postProcess(PaymentPostProcessingRequest request) {
    return PaymentPostProcessingResponse.builder().build();
  }

  public static final class PaymentPostProcessingPluginStub implements PaymentPostProcessingPlugin {
  }

  record PaymentPostProcessingRequest(PaymentPostProcessingContext context) implements PluginRequest {
    public Collection<?> structures() { return List.of(context()); }
  }
}