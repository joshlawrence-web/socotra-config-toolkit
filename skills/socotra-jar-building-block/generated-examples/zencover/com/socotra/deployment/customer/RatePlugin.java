package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin(type = PluginType.rating)
public interface RatePlugin {
  public static final Integer VERSION = 3;
  default RatingSet rate(ZenCoverQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(ZenCoverQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(ZenCoverQuickQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(ZenCoverQuickQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(ZenCoverRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(ZenCoverRequest request) {
    return rate(request);
  }

  public static final class RatePluginStub implements RatePlugin {
  }

  public static record ZenCoverQuoteRequest(ZenCoverQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator, BigDecimal duration, DurationBasis durationBasis) {
      ZenCoverQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      durationBasis = durationBasis == null ? quote.durationBasis().get() : durationBasis;
      Objects.requireNonNull(duration);
      return new ZenCoverQuoteRequest(quote, duration, durationBasis);
    }
  }
  public static record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static ZenCoverRequest of(ULID transactionLocator, BigDecimal duration, DurationBasis durationBasis) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Policy policy = dataFetcher.getPolicy(transaction.policyLocator());
      Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
      Objects.requireNonNull(duration);
      durationBasis = durationBasis == null ? policy.durationBasis() : durationBasis;
      return new ZenCoverRequest(policy, transaction, segment, duration, durationBasis);
    }
  }
}