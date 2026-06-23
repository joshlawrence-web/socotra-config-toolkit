package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

@Plugin(type = PluginType.preCommit)
public interface PreCommitPlugin {
  public static final Integer VERSION = 1;
  default BankCustomerAccount preCommit(PreCommitPlugin.BankCustomerAccountRequest request) {
    return request.account();
  }
  default BasicCreditCardProtectionQuote preCommit(PreCommitPlugin.BasicCreditCardProtectionQuoteRequest request) {
    return request.quote();
  }
  default BasicCreditCardProtectionQuickQuote preCommit(PreCommitPlugin.BasicCreditCardProtectionQuickQuoteRequest request) {
    return request.quote();
  }
  default PreCommitTransactionResponse preCommit(PreCommitPlugin.BasicCreditCardProtectionTransactionRequest request) {
    return PreCommitTransactionResponse.builder()
        .addChangeInstructions(request.changeInstructions())
        .build();
  }
  default BasicCreditCardProtectionSegment preCommit(PreCommitPlugin.BasicCreditCardProtectionRequest request) {
    return request.segment();
  }
  default PremiumCreditCardProtectionQuote preCommit(PreCommitPlugin.PremiumCreditCardProtectionQuoteRequest request) {
    return request.quote();
  }
  default PremiumCreditCardProtectionQuickQuote preCommit(PreCommitPlugin.PremiumCreditCardProtectionQuickQuoteRequest request) {
    return request.quote();
  }
  default PreCommitTransactionResponse preCommit(PreCommitPlugin.PremiumCreditCardProtectionTransactionRequest request) {
    return PreCommitTransactionResponse.builder()
        .addChangeInstructions(request.changeInstructions())
        .build();
  }
  default PremiumCreditCardProtectionSegment preCommit(PreCommitPlugin.PremiumCreditCardProtectionRequest request) {
    return request.segment();
  }
  default PreCommitDelinquencyResponse preCommit(PreCommitPlugin.DelinquencyRequest request) {
    return PreCommitDelinquencyResponse.builder()
        .settings(request.delinquency().settings())
        .build();
  }

  @Deprecated
  default PreCommitDelinquencyEventsResponse preCommit(PreCommitPlugin.DelinquencyEventsRequest request) {
    return PreCommitDelinquencyEventsResponse.builder().build();
  }

  public static final class PreCommitPluginStub implements PreCommitPlugin {
  }

  public static record BankCustomerAccountRequest(BankCustomerAccount account, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(account(), trigger()); }
    public static BankCustomerAccountRequest of(ULID accountLocator, PreCommitTrigger trigger) {
      return new BankCustomerAccountRequest(DataFetcherFactory.get().getAccount(accountLocator), trigger);
    }
  }
  public static record BasicCreditCardProtectionQuoteRequest(BasicCreditCardProtectionQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
    public static BasicCreditCardProtectionQuoteRequest of(ULID quoteLocator, PreCommitTrigger trigger) {
      return new BasicCreditCardProtectionQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), trigger);
    }
  }
  public static record BasicCreditCardProtectionQuickQuoteRequest(BasicCreditCardProtectionQuickQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
  }
  public static record BasicCreditCardProtectionTransactionRequest(Policy policy, Transaction transaction, Collection<ChangeInstructionHolder> changeInstructions, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), changeInstructions(), trigger()); }
    public static BasicCreditCardProtectionTransactionRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new BasicCreditCardProtectionTransactionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, List.of(), trigger);
    }
  }
  public static record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction, BasicCreditCardProtectionSegment segment, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment(), trigger()); }
    public static BasicCreditCardProtectionRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new BasicCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, (BasicCreditCardProtectionSegment)dataFetcher.getSegments(transaction.locator()).stream().findFirst().orElse(null), trigger);
    }
  }
  public static record PremiumCreditCardProtectionQuoteRequest(PremiumCreditCardProtectionQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
    public static PremiumCreditCardProtectionQuoteRequest of(ULID quoteLocator, PreCommitTrigger trigger) {
      return new PremiumCreditCardProtectionQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), trigger);
    }
  }
  public static record PremiumCreditCardProtectionQuickQuoteRequest(PremiumCreditCardProtectionQuickQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
  }
  public static record PremiumCreditCardProtectionTransactionRequest(Policy policy, Transaction transaction, Collection<ChangeInstructionHolder> changeInstructions, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), changeInstructions(), trigger()); }
    public static PremiumCreditCardProtectionTransactionRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new PremiumCreditCardProtectionTransactionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, List.of(), trigger);
    }
  }
  public static record PremiumCreditCardProtectionRequest(Policy policy, Transaction transaction, PremiumCreditCardProtectionSegment segment, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment(), trigger()); }
    public static PremiumCreditCardProtectionRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new PremiumCreditCardProtectionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, (PremiumCreditCardProtectionSegment)dataFetcher.getSegments(transaction.locator()).stream().findFirst().orElse(null), trigger);
    }
  }
  public static record DelinquencyRequest(Delinquency delinquency) implements PluginRequest {
    public Collection<?> structures() { return List.of(delinquency()); }
  }

  public static record DelinquencyEventsRequest(Collection<DelinquencyEvent> delinquencyEvents) implements PluginRequest {
    public Collection<?> structures() { return delinquencyEvents(); }
  }
}