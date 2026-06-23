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
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.generate));
  }
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.BasicCreditCardProtectionRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.generate));
  }
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.generate));
  }
  default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.PremiumCreditCardProtectionRequest request) {
    return request.documentConfigs().stream().collect(Collectors.toMap(DocumentConfig::name, i -> DocumentSelectionAction.generate));
  }

  public static final class DocumentSelectionPluginStub implements DocumentSelectionPlugin {
  }

  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      return new BasicCreditCardProtectionQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), documentConfigs, trigger);
    }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
      return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfigs, trigger);
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      return new PremiumCreditCardProtectionQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), documentConfigs, trigger);
    }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger) {
      Objects.requireNonNull(documentConfigs);
      Objects.requireNonNull(trigger);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
      return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfigs, trigger);
    }
  }
}
