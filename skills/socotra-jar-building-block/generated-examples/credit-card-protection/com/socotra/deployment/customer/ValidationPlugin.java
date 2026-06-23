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
  default ValidationItem validate(ValidationPlugin.BankCustomerAccountRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionQuickQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionQuickQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionQuickQuoteRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionQuickQuoteRequest request) {
    return validate(request);
  }
  default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionRequest request) {
    return ValidationItem.builder().build();
  }
  default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionRequest request) {
    return validate(request);
  }

  public static final class ValidationPluginStub implements ValidationPlugin {
  }

  public static record BankCustomerAccountRequest(BankCustomerAccount account) implements PluginRequest {
    public Collection<?> structures() { return List.of(account()); }
    public static BankCustomerAccountRequest of(ULID accountLocator) {
      BankCustomerAccount account = DataFetcherFactory.get().getAccount(accountLocator);
      return new BankCustomerAccountRequest(account);
    }
  }
  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator) {
      BasicCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new BasicCreditCardProtectionQuoteRequest(quote);
    }
  }
  public static record BasicCreditCardProtectionQuickQuoteRequest(BasicCreditCardProtectionQuickQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<BasicCreditCardProtectionSegment> segment) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<BasicCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<BasicCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(BasicCreditCardProtectionSegment::endTime));
      return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment);
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator) {
      PremiumCreditCardProtectionQuote quote = DataFetcherFactory.get().getQuote(quoteLocator);
      return new PremiumCreditCardProtectionQuoteRequest(quote);
    }
  }
  public static record PremiumCreditCardProtectionQuickQuoteRequest(PremiumCreditCardProtectionQuickQuote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, Optional<PremiumCreditCardProtectionSegment> segment) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validate state at least"); }
      Collection<PremiumCreditCardProtectionSegment> segments = dataFetcher.getSegments(transactionLocator);
      Optional<PremiumCreditCardProtectionSegment> segment = segments.stream().max(Comparator.comparing(PremiumCreditCardProtectionSegment::endTime));
      return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, segment);
    }
  }
}