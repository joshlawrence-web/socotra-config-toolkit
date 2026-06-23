package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import com.socotra.platform.tools.ULID;

@Plugin(type = PluginType.installments)
public interface InstallmentsPlugin {
  public static final Integer VERSION = 1;
  default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.ZenCoverRequest request) {
    return InstallmentsPluginResponse.builder().build();
  }

  public static final class InstallmentsPluginStub implements InstallmentsPlugin {
  }

  public static record ZenCoverRequest(
    InstallmentsPluginContext context,
    Collection<Installment> installments,
    InstallmentLattice installmentLattice) implements PluginRequest {
    public Collection<?> structures() {
      return List.of(context(), installments(), installmentLattice());
    }
    public static ZenCoverRequest of(
      InstallmentsPluginContext context,
      Collection<Installment> installments,
      InstallmentLattice installmentLattice) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(installments);
        Objects.requireNonNull(installmentLattice);
        return new ZenCoverRequest(context, installments, installmentLattice);
    }
  }

}