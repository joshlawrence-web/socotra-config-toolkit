package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

@Plugin(type = PluginType.validation)
public interface ValidationPlugin {
  public static final Integer VERSION = 3;
  default ValidationItem validate(ValidationPlugin.PersonalAccountRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem validate(ValidationPlugin.ZenCoverQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.ZenCoverQuickQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuickQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.ZenCoverRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.ZenCoverRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.StandardPaymentRequest request) {
    return ValidationItem.builder().build();
  }

  public static final class ValidationPluginStub implements ValidationPlugin {
  }

  public static record PersonalAccountRequest(PersonalAccount account) implements PluginRequest {
    public Collection<?> structures() { return List.of(account()); }
    public static PersonalAccountRequest of(ULID accountLocator) {
      PersonalAccount account = DataFetcherFactory.get().getAccount(accountLocator);
      return new PersonalAccountRequest(account);
    }
  }
  public static record ZenCoverQuoteRequest(ZenCoverQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator) {
      ZenCoverQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new ZenCoverQuoteRequest(quote);
    }
  }
  public static record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static ZenCoverRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<ZenCoverSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<ZenCoverSegment> segment = segments.stream().max(Comparator.comparing(ZenCoverSegment::endTime));
      return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment);
    }
  }
  public static record StandardPaymentRequest(StandardPayment payment) implements PluginRequest {
    public Collection<?> structures() { return List.of(payment()); }
  }
}