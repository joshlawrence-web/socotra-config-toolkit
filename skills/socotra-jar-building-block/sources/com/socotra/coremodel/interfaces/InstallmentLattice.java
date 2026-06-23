package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.InstallmentLatticeFrame;
import com.socotra.coremodel.LatticeCategory;
import com.socotra.coremodel.views.Internal;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface InstallmentLattice {
  ULID locator();

  Optional<ULID> settingsLocator();

  Instant createdAt();

  UUID createdBy();

  ULID accountLocator();

  Instant termStartTime();

  Instant termEndTime();

  Optional<ULID> termLocator();

  Optional<ULID> quoteLocator();

  Optional<ULID> policyLocator();

  String currency();

  String timezone();

  Optional<ULID> basedOnLocator();

  Instant effectiveTime();

  // The creatorTransactionLocator points to the transaction which led to the creation of the
  // installment lattice. It might point to the original requested transaction in case of
  // multi-term billing mode change. It gets updated if a transaction with billing mode change gets
  // reapplied for later OOS transactions
  @JsonView(Internal.class)
  Optional<ULID> creatorTransactionLocator();

  // The lattice category will be `normal` unless the lattice was created for a `reversal`
  // transaction or the lattice is being reversed because of the `reversal` transaction .In the
  // later case, the `reversedLatticeLocators` and `reReversedLatticeLocators` will be empty to
  // signify that the category of a normal lattice was changed to reversal. This can go back and
  // forth.
  @JsonView(Internal.class)
  LatticeCategory latticeCategory();

  // The reversedLatticeLocators will be populated when a new lattice gets created for `reversal`
  // transaction. The list will contain the locators of all the `normal` lattices which are needed
  // to be reversed.
  @JsonView(Internal.class)
  Collection<ULID> reversedLatticeLocators();

  // The reReversedLatticeLocators will be populated when a new lattice gets created for
  // `reversal` transaction reversing a reversal transaction. The operation reversal of
  // reversal will move the locators from reversalLatticeLocators to reReversedLatticeLocators, and
  // the lattice category will change back to `normal` for these lattices
  @JsonView(Internal.class)
  Collection<ULID> reReversedLatticeLocators();

  Collection<InstallmentLatticeFrame> frames();
}
