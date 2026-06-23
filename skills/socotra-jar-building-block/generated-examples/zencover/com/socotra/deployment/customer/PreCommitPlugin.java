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
  default PersonalAccount preCommit(PreCommitPlugin.PersonalAccountRequest request) {
    return request.account();
  }
  default ZenCoverQuote preCommit(PreCommitPlugin.ZenCoverQuoteRequest request) {
    return request.quote();
  }
  default ZenCoverQuickQuote preCommit(PreCommitPlugin.ZenCoverQuickQuoteRequest request) {
    return request.quote();
  }
  default PreCommitTransactionResponse preCommit(PreCommitPlugin.ZenCoverTransactionRequest request) {
    return PreCommitTransactionResponse.builder()
        .addChangeInstructions(request.changeInstructions())
        .build();
  }
  default ZenCoverSegment preCommit(PreCommitPlugin.ZenCoverRequest request) {
    return request.segment();
  }
  default StandardPayment preCommit(PreCommitPlugin.StandardPaymentRequest request) {
    return request.payment();
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

  public static record PersonalAccountRequest(PersonalAccount account, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(account(), trigger()); }
    public static PersonalAccountRequest of(ULID accountLocator, PreCommitTrigger trigger) {
      return new PersonalAccountRequest(DataFetcherFactory.get().getAccount(accountLocator), trigger);
    }
  }
  public static record ZenCoverQuoteRequest(ZenCoverQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
    public static ZenCoverQuoteRequest of(ULID quoteLocator, PreCommitTrigger trigger) {
      return new ZenCoverQuoteRequest(DataFetcherFactory.get().getQuote(quoteLocator), trigger);
    }
  }
  public static record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote(), trigger()); }
  }
  public static record ZenCoverTransactionRequest(Policy policy, Transaction transaction, Collection<ChangeInstructionHolder> changeInstructions, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), changeInstructions(), trigger()); }
    public static ZenCoverTransactionRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new ZenCoverTransactionRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, List.of(), trigger);
    }
  }
  public static record ZenCoverRequest(Policy policy, Transaction transaction, ZenCoverSegment segment, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), transaction(), segment(), trigger()); }
    public static ZenCoverRequest of(ULID transactionLocator, PreCommitTrigger trigger) {
      DataFetcher dataFetcher = DataFetcherFactory.get();
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.transactionState().equals(TransactionState.draft)) { throw new IllegalStateException("Cannot use transaction in draft state. Please advance the transaction to validated state at least"); }
      // TODO
      return new ZenCoverRequest(dataFetcher.getPolicy(transaction.policyLocator()), transaction, (ZenCoverSegment)dataFetcher.getSegments(transaction.locator()).stream().findFirst().orElse(null), trigger);
    }
  }
  public static record StandardPaymentRequest(StandardPayment payment, PreCommitTrigger trigger) implements PluginRequest {
    public Collection<?> structures() { return List.of(payment(), trigger()); }
  }
  public static record DelinquencyRequest(Delinquency delinquency) implements PluginRequest {
    public Collection<?> structures() { return List.of(delinquency()); }
  }

  public static record DelinquencyEventsRequest(Collection<DelinquencyEvent> delinquencyEvents) implements PluginRequest {
    public Collection<?> structures() { return delinquencyEvents(); }
  }
}