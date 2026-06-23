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
  default RatingSet rate(BasicCreditCardProtectionQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(BasicCreditCardProtectionQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(BasicCreditCardProtectionQuickQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(BasicCreditCardProtectionQuickQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(BasicCreditCardProtectionRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(BasicCreditCardProtectionRequest request) {
    return rate(request);
  }
  default RatingSet rate(PremiumCreditCardProtectionQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(PremiumCreditCardProtectionQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(PremiumCreditCardProtectionQuickQuoteRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(PremiumCreditCardProtectionQuickQuoteRequest request) {
    return rate(request);
  }
  default RatingSet rate(PremiumCreditCardProtectionRequest request) {
    return RatingSet.builder().ok(true).ratingItems(Collections.emptyList()).build();
  }
  default RatingSet statelessRate(PremiumCreditCardProtectionRequest request) {
    return rate(request);
  }

  public static final class RatePluginStub implements RatePlugin {
  }

  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator, BigDecimal duration, DurationBasis durationBasis) {
      BasicCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      durationBasis = durationBasis == null ? quote.durationBasis().get() : durationBasis;
      Objects.requireNonNull(duration);
      return new BasicCreditCardProtectionQuoteRequest(quote, duration, durationBasis);
    }
  }
  public static record BasicCreditCardProtectionQuickQuoteRequest(BasicCreditCardProtectionQuickQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator, BigDecimal duration, DurationBasis durationBasis) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Policy policy = dataFetcher.getPolicy(transaction.policyLocator());
      Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
      Objects.requireNonNull(duration);
      durationBasis = durationBasis == null ? policy.durationBasis() : durationBasis;
      return new BasicCreditCardProtectionRequest(policy, transaction, segment, duration, durationBasis);
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator, BigDecimal duration, DurationBasis durationBasis) {
      PremiumCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      durationBasis = durationBasis == null ? quote.durationBasis().get() : durationBasis;
      Objects.requireNonNull(duration);
      return new PremiumCreditCardProtectionQuoteRequest(quote, duration, durationBasis);
    }
  }
  public static record PremiumCreditCardProtectionQuickQuoteRequest(PremiumCreditCardProtectionQuickQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator, BigDecimal duration, DurationBasis durationBasis) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Policy policy = dataFetcher.getPolicy(transaction.policyLocator());
      Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
      Objects.requireNonNull(duration);
      durationBasis = durationBasis == null ? policy.durationBasis() : durationBasis;
      return new PremiumCreditCardProtectionRequest(policy, transaction, segment, duration, durationBasis);
    }
  }
}