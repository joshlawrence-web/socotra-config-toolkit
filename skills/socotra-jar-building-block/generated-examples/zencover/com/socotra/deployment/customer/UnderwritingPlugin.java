package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.util.stream.Stream;

@Plugin(type = PluginType.underwriting)
public interface UnderwritingPlugin {
  public static final Integer VERSION = 2;
  default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverQuoteRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverQuoteRequest request) {
    return underwrite(request);
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverRequest request) {
    return underwrite(request);
  }

  public static final class UnderwritingPluginStub implements UnderwritingPlugin {
  }

  public static record ZenCoverQuoteRequest(ZenCoverQuote quote, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      ZenCoverQuote quote = dataFetcher.getQuote(quoteLocator);
      // TODO Collection<UnderwritingFlag> flags = dataFetcher.getQuoteUnderwritingFlags(quoteLocator).flags();
      return new ZenCoverQuoteRequest(quote, List.of());
    }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static ZenCoverRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
      return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, /* TODO dataFetcher.getTransactionUnderwritingFlags(transactionLocator).flags() */ List.of());
    }
  }
}