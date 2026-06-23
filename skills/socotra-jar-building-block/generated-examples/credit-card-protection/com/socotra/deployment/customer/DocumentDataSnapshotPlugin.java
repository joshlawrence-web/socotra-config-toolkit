package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.documentDataSnapshot)
public interface DocumentDataSnapshotPlugin {
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.quote())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.BasicCreditCardProtectionRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.segment().isEmpty() ? Map.of() : request.segment())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.quote())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.PremiumCreditCardProtectionRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.segment().isEmpty() ? Map.of() : request.segment())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.InvoiceDetailsRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.invoiceDetails() == null ? Map.of() : request.invoiceDetails())
        .build();
  }

  public static final class DocumentDataSnapshotPluginStub implements DocumentDataSnapshotPlugin {
  }

  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      BasicCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new BasicCreditCardProtectionQuoteRequest(quote, documentConfig);
    }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
      return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig);
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      PremiumCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new PremiumCreditCardProtectionQuoteRequest(quote, documentConfig);
    }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
      return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig);
    }
  }
  public static record InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(invoiceDetails()); }
    public static InvoiceDetailsRequest of(ULID invoiceLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      InvoiceDetails invoiceDetails = dataFetcher.getInvoiceDetails(invoiceLocator);
      return new InvoiceDetailsRequest(invoiceDetails, documentConfig);
    }
  }
}
