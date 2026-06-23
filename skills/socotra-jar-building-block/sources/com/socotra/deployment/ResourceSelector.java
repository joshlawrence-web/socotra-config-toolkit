package com.socotra.deployment;

import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.QuoteCore;
import com.socotra.coremodel.interfaces.SelectableResource;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface ResourceSelector {

  static ResourceSelector get(Object forObject) {
    return ResourceSelectorFactory.getInstance().getSelector(forObject);
  }

  <T extends TableMetadata & CustomerObject> TableRecordFetcher<T> getTable(Class<T> tableType);

  <T extends RangeTableMetadata & CustomerObject> RangeTableRecordFetcher<T> getRangeTable(
      Class<T> tableType);

  ConstraintsFetcher getConstraints(Class<? extends ConstraintTableMetadata> tableType);

  <T> Optional<T> getSecret(Class<T> secretType);

  /**
   * Select time using SelectionTimeBasis for provided object.
   *
   * @param basis
   * @param o
   * @return
   */
  static Instant selectionTime(SelectionTimeBasis basis, Object o) {
    return basis.selectionTime(
        switch (basis) {
          case termStartTime -> {
            if (o instanceof Transaction transaction) {
              yield switch (transaction.transactionCategory()) {
                case issuance, renewal -> transaction;
                default -> DataFetcherFactory.get().getTerm(transaction.termLocator());
              };
            }
            yield o;
          }
          case policyStartTime -> {
            if (o instanceof Transaction transaction) {
              if (transaction.locator().equals(transaction.policyLocator())) {
                yield transaction;
              }
              yield DataFetcherFactory.get().getPolicy(transaction.policyLocator());
            }
            yield o;
          }
          default -> o;
        });
  }

  static Optional<String> jurisdiction(Object object) {
    return switch (object) {
      case Policy policy -> policy.jurisdiction();
      case QuoteCore quote -> quote.jurisdiction();
      case Transaction transaction ->
          jurisdiction(DataFetcherFactory.get().getPolicy(transaction.policyLocator()));
      case com.socotra.coremodel.interfaces.Segment segment ->
          jurisdiction(DataFetcherFactory.get().getTransaction(segment.transactionLocator()));
      case Term term -> jurisdiction(DataFetcherFactory.get().getPolicy(term.policyLocator()));
      default -> Optional.empty();
    };
  }

  static Map.Entry<String, SelectionTimeBasis> extractStaticNameAndBasis(
      Class<? extends SelectableResource> tableClass) {
    try {
      String staticName = (String) tableClass.getMethod("getStaticName").invoke(null);
      SelectionTimeBasis basis =
          (SelectionTimeBasis) tableClass.getMethod("getSelectionTimeBasis").invoke(null);
      return Map.entry(staticName, basis);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to extract staticName and selectionTimeBasis from " + tableClass.getName());
    }
  }
}
