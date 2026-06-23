package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.documentConsolidationSelection)
public interface DocumentConsolidationSelectionPlugin {
    public static final Integer VERSION = 1;

    default List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverQuoteRequest request) {
        return request.documents().stream()
            .map(DocumentSummary::locator)
            .toList();
    }

    default List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverRequest request) {
        return request.documents().stream()
            .map(DocumentSummary::locator)
            .toList();
    }

    public static final class DocumentConsolidationSelectionPluginStub implements DocumentConsolidationSelectionPlugin {
    }

    public static record ZenCoverQuoteRequest(ZenCoverQuote quote,
                                            ConsolidatedDocumentConfig config,
                                            Collection<DocumentSummary> documents,
                                            String productName) implements PluginRequest {
        public Collection<?> structures() { return List.of(quote()); }
        public static ZenCoverQuoteRequest of(ULID quoteLocator,
                                            ConsolidatedDocumentConfig config,
                                            Collection<DocumentSummary> documents,
                                            String productName) {
            Objects.requireNonNull(config);
            ZenCoverQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
            return new ZenCoverQuoteRequest(quote, config, documents, productName);
        }
    }

    public static record ZenCoverRequest(Policy policy,
                                        Transaction transaction,
                                        Optional<ZenCoverSegment> segment,
                                        ConsolidatedDocumentConfig config,
                                        Collection<DocumentSummary> documents) implements PluginRequest {
            public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
            public static ZenCoverRequest of(ULID transactionLocator,
                                            ConsolidatedDocumentConfig config,
                                            Collection<DocumentSummary> documents) {
                Objects.requireNonNull(config);
                DataFetcher dataFetcher = DataFetcherFactory.get();
                Transaction transaction = dataFetcher.getTransaction(transactionLocator);
                if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
                Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
                Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
                return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, config, documents);
            }
    }
}
