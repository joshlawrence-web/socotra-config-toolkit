package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.documentConsolidationSnapshot)
public interface DocumentConsolidationSnapshotPlugin {
    public static final Integer VERSION = 1;
    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.ZenCoverQuoteRequest request) {
        return DocumentDataSnapshot.builder()
                .renderingData(request.quote())
                .build();
    }

    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.ZenCoverRequest request) {
        return DocumentDataSnapshot.builder()
            .renderingData(request.segment().isEmpty() ? request.transaction() : request.segment())
            .build();
    }
    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.InvoiceDetailsRequest request) {
        return DocumentDataSnapshot.builder()
            .renderingData(request.invoiceDetails() == null ? Map.of("invoiceLocator", "not_found") : request.invoiceDetails())
            .build();
    }

    public static final class DocumentConsolidationSnapshotPluginStub implements DocumentConsolidationSnapshotPlugin {
    }

    public static record ZenCoverQuoteRequest(ZenCoverQuote quote, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(quote()); }
        public static ZenCoverQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            ZenCoverQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
            return new ZenCoverQuoteRequest(quote, documentConfig, consolidationInfo);
        }
        public ZenCoverQuoteRequest(ZenCoverQuote quote, DocumentConsolidationInfo consolidationInfo) {
            this(quote, null, consolidationInfo);
        }
    }
    public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
        public static ZenCoverRequest of(ULID transactionLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            DataFetcher dataFetcher = DataFetcherFactory.get();
            Transaction transaction = dataFetcher.getTransaction(transactionLocator);
            if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
            Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
            Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
            return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig, consolidationInfo);
        }
        public ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, DocumentConsolidationInfo consolidationInfo) {
            this(policy, transaction, segment, null, consolidationInfo);
        }
    }
    public static record InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(invoiceDetails()); }
        public static InvoiceDetailsRequest of(ULID invoiceLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            DataFetcher dataFetcher = DataFetcherFactory.get();
            InvoiceDetails invoiceDetails = dataFetcher.getInvoiceDetails(invoiceLocator);
            return new InvoiceDetailsRequest(invoiceDetails, documentConfig, consolidationInfo);
        }
        public InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConsolidationInfo consolidationInfo) {
            this(invoiceDetails, null, consolidationInfo);
        }
    }
}