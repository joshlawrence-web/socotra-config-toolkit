package com.socotra.developer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.util.*;

public final class APIDataFetcher extends InternalAPIDataFetcher {
  private final DeploymentFactory deploymentFactory;

  public APIDataFetcher(APIClient apiClient) {
    super(apiClient);
    deploymentFactory = apiClient.factory();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAccount(ULID accountLocator) {
    Account account = super.getAccount(accountLocator);
    return account.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getQuickQuote(ULID quickQuoteLocator) {
    QuickQuote quickQuote = super.getQuickQuote(quickQuoteLocator);
    return quickQuote.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getQuote(ULID quoteLocator) {
    Quote quote = super.getQuote(quoteLocator);
    return quote.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<T> getSegments(ULID transactionLocator) {
    Collection<Segment> segments = super.getSegments(transactionLocator);
    return segments.stream().map(s -> (T) s.toCustomerObject(deploymentFactory)).toList();
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.Fnol<?>> T getFnol(ULID fnolLocator) {
    Fnol fnol = super.getFnol(fnolLocator);
    return fnol.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getSegmentByTransaction(ULID transactionLocator) {
    Segment segment = super.getSegmentByTransaction(transactionLocator);
    if (segment == null) {
      return null;
    }
    return segment.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getSegment(ULID segmentLocator) {
    Segment segment = super.getSegment(segmentLocator);
    return segment.toCustomerObject(deploymentFactory);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getPayment(ULID paymentLocator) {
    PaymentObject payment = super.getPayment(paymentLocator);
    return payment.toCustomerObject(deploymentFactory);
  }
}
