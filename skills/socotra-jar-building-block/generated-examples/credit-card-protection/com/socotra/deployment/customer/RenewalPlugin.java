package com.socotra.deployment.customer;

import com.socotra.coremodel.*;

import java.util.*;
import java.util.List;

@Plugin(type = PluginType.renewal)
public interface RenewalPlugin {

  default AutoRenewalResponse renew(AutoRenewalRequest request) {
    AutoRenewal autoRenewal = request.autoRenewal();
    return AutoRenewalResponse.builder()
        .autoRenewalState(autoRenewal.autoRenewalState())
        .renewalTransactionType(autoRenewal.renewalTransactionType())
        .renewalTransactionCreateTime(autoRenewal.renewalTransactionCreateTime())
        .renewalTransactionIssueTime(autoRenewal.renewalTransactionIssueTime())
        .renewalTransactionAcceptTime(autoRenewal.renewalTransactionAcceptTime())
        .build();
  }

  public static final class RenewalPluginStub implements RenewalPlugin {
  }

  record AutoRenewalRequest(AutoRenewalEvent event, AutoRenewal autoRenewal) implements PluginRequest {
    public Collection<?> structures() { return List.of(autoRenewal()); }
  }
}