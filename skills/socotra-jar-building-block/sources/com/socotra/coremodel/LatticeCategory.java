package com.socotra.coremodel;

public enum LatticeCategory {
  // The default category `normal` represents that the current lattice is not involved in any
  // reversal transaction since created
  normal,
  // The category `reversal` represents that either the installment lattice in context has been
  // reversed by
  // one of the transaction marked with TransactionCategory.reversal or the lattice was created
  // because of the
  // explicit reversal transaction
  reversal,
  // the category migration represents that the lattice is created as part of the migration process
  // and all billed installments will exclude from the billing mode change process
  migration,
}
