package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.util.stream.Stream;

@Plugin(type = PluginType.underwriting)
public interface UnderwritingPlugin {
  public static final Integer VERSION = 2;
  default UnderwritingModification underwrite(UnderwritingPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification underwrite(UnderwritingPlugin.BasicCreditCardProtectionRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return underwrite(request);
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.BasicCreditCardProtectionRequest request) {
    return underwrite(request);
  }
  default UnderwritingModification underwrite(UnderwritingPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification underwrite(UnderwritingPlugin.PremiumCreditCardProtectionRequest request) {
    return UnderwritingModification.builder().build();
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return underwrite(request);
  }
  default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.PremiumCreditCardProtectionRequest request) {
    return underwrite(request);
  }

  public static final class UnderwritingPluginStub implements UnderwritingPlugin {
  }

  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      BasicCreditCardProtectionQuote quote = dataFetcher.getQuote(quoteLocator);
      // TODO Collection<UnderwritingFlag> flags = dataFetcher.getQuoteUnderwritingFlags(quoteLocator).flags();
      return new BasicCreditCardProtectionQuoteRequest(quote, List.of());
    }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
      return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, /* TODO dataFetcher.getTransactionUnderwritingFlags(transactionLocator).flags() */ List.of());
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      PremiumCreditCardProtectionQuote quote = dataFetcher.getQuote(quoteLocator);
      // TODO Collection<UnderwritingFlag> flags = dataFetcher.getQuoteUnderwritingFlags(quoteLocator).flags();
      return new PremiumCreditCardProtectionQuoteRequest(quote, List.of());
    }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment, Collection<UnderwritingFlag> flags) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
      return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment, /* TODO dataFetcher.getTransactionUnderwritingFlags(transactionLocator).flags() */ List.of());
    }
  }
}