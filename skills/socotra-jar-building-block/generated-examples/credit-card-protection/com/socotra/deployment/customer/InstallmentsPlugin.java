package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import com.socotra.platform.tools.ULID;

@Plugin(type = PluginType.installments)
public interface InstallmentsPlugin {
  default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.BasicCreditCardProtectionRequest request) {
    return InstallmentsPluginResponse.builder().build();
  }

  default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.PremiumCreditCardProtectionRequest request) {
    return InstallmentsPluginResponse.builder().build();
  }

  public static final class InstallmentsPluginStub implements InstallmentsPlugin {
  }

  public static record BasicCreditCardProtectionRequest(
    InstallmentsPluginContext context,
    Collection<Installment> installments,
    InstallmentLattice installmentLattice) implements PluginRequest {
    public Collection<?> structures() { return List.of(installments(), installmentLattice()); }
    public static BasicCreditCardProtectionRequest of(
      InstallmentsPluginContext context,
      Collection<Installment> installments,
      InstallmentLattice installmentLattice) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(installments);
        Objects.requireNonNull(installmentLattice);
        return new BasicCreditCardProtectionRequest(context, installments, installmentLattice);
    }
  }

  public static record PremiumCreditCardProtectionRequest(
    InstallmentsPluginContext context,
    Collection<Installment> installments,
    InstallmentLattice installmentLattice) implements PluginRequest {
    public Collection<?> structures() { return List.of(installments(), installmentLattice()); }
    public static PremiumCreditCardProtectionRequest of(
      InstallmentsPluginContext context,
      Collection<Installment> installments,
      InstallmentLattice installmentLattice) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(installments);
        Objects.requireNonNull(installmentLattice);
        return new PremiumCreditCardProtectionRequest(context, installments, installmentLattice);
    }
  }

}