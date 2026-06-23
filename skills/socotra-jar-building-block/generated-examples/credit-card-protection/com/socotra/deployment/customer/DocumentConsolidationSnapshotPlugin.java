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
    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionQuoteRequest request) {
        return DocumentDataSnapshot.builder()
                .renderingData(request.quote())
                .build();
    }

    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionRequest request) {
        return DocumentDataSnapshot.builder()
            .renderingData(request.segment().isEmpty() ? Map.of() : request.segment())
            .build();
    }
    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest request) {
        return DocumentDataSnapshot.builder()
                .renderingData(request.quote())
                .build();
    }

    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionRequest request) {
        return DocumentDataSnapshot.builder()
            .renderingData(request.segment().isEmpty() ? Map.of() : request.segment())
            .build();
    }
    default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.InvoiceDetailsRequest request) {
        return DocumentDataSnapshot.builder()
            .renderingData(request.invoiceDetails() == null ? Map.of() : request.invoiceDetails())
            .build();
    }

    public static final class DocumentConsolidationSnapshotPluginStub implements DocumentConsolidationSnapshotPlugin {
    }

    public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(quote()); }
        public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            Objects.requireNonNull(documentConfig);
            BasicCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
            return new BasicCreditCardProtectionQuoteRequest(quote, documentConfig, consolidationInfo);
        }
    }
    public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
        public static BasicCreditCardProtectionRequest of(ULID transactionLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            Objects.requireNonNull(documentConfig);
            DataFetcher dataFetcher = DataFetcherFactory.get();
            Transaction transaction = dataFetcher.getTransaction(transactionLocator);
            if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
            Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
            Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
            return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig, consolidationInfo);
        }
    }
    public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(quote()); }
        public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            Objects.requireNonNull(documentConfig);
            PremiumCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
            return new PremiumCreditCardProtectionQuoteRequest(quote, documentConfig, consolidationInfo);
        }
    }
    public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
        public static PremiumCreditCardProtectionRequest of(ULID transactionLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            Objects.requireNonNull(documentConfig);
            DataFetcher dataFetcher = DataFetcherFactory.get();
            Transaction transaction = dataFetcher.getTransaction(transactionLocator);
            if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
            Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
            Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
            return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, documentConfig, consolidationInfo);
        }
    }
    public static record InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConfig config, DocumentConsolidationInfo consolidationInfo) implements PluginRequest {
        public Collection<?> structures() { return List.of(invoiceDetails()); }
        public static InvoiceDetailsRequest of(ULID invoiceLocator, DocumentConfig documentConfig, DocumentConsolidationInfo consolidationInfo) {
            Objects.requireNonNull(documentConfig);
            DataFetcher dataFetcher = DataFetcherFactory.get();
            InvoiceDetails invoiceDetails = dataFetcher.getInvoiceDetails(invoiceLocator);
            return new InvoiceDetailsRequest(invoiceDetails, documentConfig, consolidationInfo);
        }
    }
}
