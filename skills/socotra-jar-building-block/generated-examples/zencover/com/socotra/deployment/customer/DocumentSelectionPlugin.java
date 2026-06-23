package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.documentSelection)
public interface DocumentSelectionPlugin {
  public static final Integer VERSION = 1;
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverQuoteRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.defaultAction));
  }
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.defaultAction));
  }

  public static final class DocumentSelectionPluginStub implements DocumentSelectionPlugin {
  }

  public static record ZenCoverQuoteRequest(ZenCoverQuote quote, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      return new ZenCoverQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), documentConfigs, trigger);
    }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static ZenCoverRequest of(ULID transactionLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
      return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfigs, trigger);
    }
  }
}