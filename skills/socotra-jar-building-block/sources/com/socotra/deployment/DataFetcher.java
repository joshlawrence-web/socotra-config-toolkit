package com.socotra.deployment;

import com.socotra.coremodel.*;
import com.socotra.coremodel.Invoice;
import com.socotra.platform.tools.ULID;
import java.util.Collection;
import java.util.Map;

public interface DataFetcher {
  <T> T getAccount(ULID accountLocator);

  <T> T getQuickQuote(ULID quickQuoteLocator);

  <T> T getQuote(ULID quoteLocator);

  QuoteGroup getQuoteGroup(ULID quoteGroupLocator);

  UnderwritingFlags getQuoteUnderwritingFlags(ULID quoteLocator);

  UnderwritingFlags getTransactionUnderwritingFlags(ULID transactionLocator);

  UnderwritingFlag getUnderwritingFlag(ULID underwritingFlagLocator);

  QuotePricing getQuotePricing(ULID quoteLocator);

  Transaction getTransaction(ULID transactionLocator);

  Policy getPolicy(ULID policyLocator);

  Task getTask(ULID taskLocator);

  UserAssociation getUserAssociation(ULID userAssociationLocator);

  @Deprecated
  <T> Collection<T> getSegments(ULID transactionLocator);

  <T> T getSegmentByTransaction(ULID transactionLocator);

  <T> T getSegment(ULID segmentLocator);

  AuxData getAuxData(String locator, String key);

  AuxDataKeysSet getAuxDataKeys(String locator, int offset, int count);

  Collection<DiaryEntry> getDiaries(
      DiaryReferenceType referenceType, ULID referenceLocator, int offset, int count);

  TransactionPricing getTransactionPricing(ULID transactionLocator);

  Collection<AffectedTransaction> getAffectedTransactions(ULID transactionLocator);

  Term getTerm(ULID termLocator);

  Map<ULID, Collection<Charge>> getTermCharges(ULID termLocator);

  Collection<DocumentInstance> getQuoteDocuments(ULID quoteLocator);

  Collection<DocumentInstance> getSegmentDocuments(ULID segmentLocator);

  Collection<DocumentInstance> getDocumentsAttachedToTransaction(ULID transactionLocator);

  Preferences getPreferences(ULID transactionLocator);

  <T extends CustomerObject> T getQuoteStaticData(ULID locator);

  <T extends CustomerObject> T getPolicyStaticData(ULID locator);

  Invoice getInvoice(ULID invoiceLocator);

  <T> T getPayment(ULID paymentLocator);

  Collection<DelinquencyEvent> getDelinquencyEvents(ULID delinquencyLocator, int offset, int count);

  static DataFetcher getInstance() {
    return DataFetcherFactory.get();
  }

  InvoiceDetails getInvoiceDetails(ULID invoiceLocator);

  <T extends com.socotra.coremodel.interfaces.Fnol<?>> T getFnol(ULID fnolLocator);

  Collection<FnolLoss> getFnolLosses(ULID fnolLocator);

  Collection<ULID> getFnolClaims(ULID fnolLocator);

  Collection<ContactRoles> getFnolContacts(ULID fnolLocator);

  <T extends com.socotra.coremodel.interfaces.Contact<?>> T getContact(ULID locator);

  Installment getInstallment(ULID installmentLocator);

  InstallmentLattice getInstallmentLattice(ULID installmentLatticeLocator);

  StreamingEntity<SubsegmentSummary> getTermSubsegmentSummaries(ULID termLocator);

  <T extends com.socotra.coremodel.interfaces.Producer<?>> T getProducer(ULID producerLocator);

  <T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(
      ULID producerCodeLocator);

  <T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(String code);

  <T extends com.socotra.coremodel.interfaces.ProducerLicense<?>> T getProducerLicense(
      ULID producerLicenseLocator);

  <T extends com.socotra.coremodel.interfaces.ProducerAppointment<?>> T getProducerAppointment(
      ULID producerAppointmentLocator);
}
