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
  public static final Integer VERSION = 1;
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverQuoteRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.quote())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.segment().isEmpty() ? request.transaction() : request.segment())
        .build();
  }
  default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.InvoiceDetailsRequest request) {
    return DocumentDataSnapshot.builder()
        .renderingData(request.invoiceDetails() == null ? Map.of("invoiceLocator", "not_found") : request.invoiceDetails())
        .build();
  }

  public static final class DocumentDataSnapshotPluginStub implements DocumentDataSnapshotPlugin {
  }

  public static record ZenCoverQuoteRequest(ZenCoverQuote quote, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      ZenCoverQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new ZenCoverQuoteRequest(quote, documentConfig);
    }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, DocumentConfig config) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static ZenCoverRequest of(ULID transactionLocator, DocumentConfig documentConfig) {
      Objects.requireNonNull(documentConfig);
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
      return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig);
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