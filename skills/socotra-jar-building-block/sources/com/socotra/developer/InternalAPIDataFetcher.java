package com.socotra.developer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socotra.coremodel.*;
import com.socotra.deployment.DataFetcher;
import com.socotra.deployment.StreamingEntity;
import com.socotra.platform.tools.ULID;
import java.util.*;
import java.util.stream.Stream;

public class InternalAPIDataFetcher implements DataFetcher {
  private static InternalAPIDataFetcher INSTANCE;
  private final APIClient apiClient;

  protected InternalAPIDataFetcher(APIClient apiClient) {
    this.apiClient = apiClient;
    new InternalAPIDataFetcher(this);
  }

  private InternalAPIDataFetcher(InternalAPIDataFetcher other) {
    this.apiClient = other.apiClient;
    INSTANCE = this;
  }

  /** For testing purposes only. */
  public static InternalAPIDataFetcher getInstance() {
    return INSTANCE;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAccount(ULID accountLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createAccountGetRequest(accountLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, Account.class));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getQuickQuote(ULID quickQuoteLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuickQuoteGetRequest(quickQuoteLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, QuickQuote.class));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getQuote(ULID quoteLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuoteGetRequest(quoteLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, Quote.class));
  }

  @Override
  public QuoteGroup getQuoteGroup(ULID quoteGroupLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuoteGroupGetRequest(quoteGroupLocator), QuoteGroup.class);
  }

  @Override
  public UnderwritingFlags getQuoteUnderwritingFlags(ULID quoteLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuoteUnderwritingFlagGetRequest(quoteLocator), UnderwritingFlags.class);
  }

  @Override
  public UnderwritingFlags getTransactionUnderwritingFlags(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionUnderwritingFlagGetRequest(transactionLocator),
        UnderwritingFlags.class);
  }

  @Override
  public UnderwritingFlag getUnderwritingFlag(ULID underwritingFlagLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createUnderwritingFlagGetRequest(underwritingFlagLocator),
        UnderwritingFlag.class);
  }

  @Override
  public QuotePricing getQuotePricing(ULID quoteLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuotePricingGetRequest(quoteLocator), QuotePricing.class);
  }

  @Override
  public Transaction getTransaction(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionGetRequest(transactionLocator), Transaction.class);
  }

  @Override
  public Policy getPolicy(ULID policyLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createPolicyGetBaseRequest("policies/" + policyLocator).build(), Policy.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<T> getSegments(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionSegmentGetRequest(transactionLocator),
        is -> {
          Segment segment =
              apiClient.factory().getObjectMapper().readValue(is, new TypeReference<>() {});
          if (segment == null) {
            return List.of();
          }
          return List.of((T) segment);
        });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends com.socotra.coremodel.interfaces.Fnol<?>> T getFnol(ULID fnolLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createFnolGetRequest(fnolLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, Fnol.class));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getSegmentByTransaction(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionSegmentGetRequest(transactionLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, Segment.class));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getSegment(ULID segmentLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createSegmentGetRequest(segmentLocator),
        is -> (T) apiClient.factory().getObjectMapper().readValue(is, Segment.class));
  }

  @Override
  public AuxData getAuxData(String locator, String key) {
    return apiClient.executeAPIRequest(
        apiClient.createAuxDataGetBaseRequest(locator + "/" + key).build(), AuxData.class);
  }

  @Override
  public AuxDataKeysSet getAuxDataKeys(String locator, int offset, int count) {
    return apiClient.executeAPIRequest(
        apiClient
            .createAuxDataGetBaseRequest(locator + "?offset=" + offset + "&count=" + count)
            .build(),
        AuxDataKeysSet.class);
  }

  @Override
  public Collection<DiaryEntry> getDiaries(
      DiaryReferenceType referenceType, ULID referenceLocator, int offset, int count) {
    return apiClient.executeAPIRequest(
        apiClient
            .createAuxDataGetBaseRequest(
                "diary/"
                    + referenceType
                    + "/"
                    + referenceLocator
                    + "?offset="
                    + offset
                    + "&count="
                    + count)
            .build(),
        new TypeReference<>() {});
  }

  /** API response is different from core-datamodel object. */
  @SuppressWarnings("unchecked")
  private TransactionPricing toTransactionPricing(Map<String, Object> map) {
    ObjectMapper mapper = apiClient.factory().getObjectMapper();
    Collection<Map<String, Object>> items =
        (Collection<Map<String, Object>>) map.getOrDefault("charges", List.of());
    Collection<Map<String, Object>> aggregated =
        (Collection<Map<String, Object>>) map.getOrDefault("aggregatedTransactions", List.of());
    return TransactionPricing.builder()
        .transactionLocator(
            // May throw IllegalArgumentException with clear message later if value is null or
            // invalid
            (ULID.from((String) map.get("locator"))))
        .items(items.stream().map(o -> mapper.convertValue(o, Charge.class)).toList())
        .aggregated(aggregated.stream().map(this::toTransactionPricing).toList())
        .build();
  }

  @Override
  public TransactionPricing getTransactionPricing(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionPriceGetRequest(transactionLocator),
        is ->
            toTransactionPricing(
                apiClient.factory().getObjectMapper().readValue(is, new TypeReference<>() {})));
  }

  @Override
  public Collection<AffectedTransaction> getAffectedTransactions(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTransactionAffectedListGetRequest(transactionLocator),
        new TypeReference<>() {});
  }

  @Override
  public Term getTerm(ULID termLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTermsBaseRequest(termLocator.toString()).build(), Term.class);
  }

  @Override
  public Map<ULID, Collection<Charge>> getTermCharges(ULID termLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createTermsBaseRequest(termLocator + "/charges").build(),
        new TypeReference<>() {});
  }

  @Override
  public Collection<DocumentInstance> getQuoteDocuments(ULID quoteLocator) {
    return apiClient.getQuoteDocuments(quoteLocator);
  }

  @Override
  public Collection<DocumentInstance> getSegmentDocuments(ULID segmentLocator) {
    return apiClient.getSegmentDocuments(segmentLocator);
  }

  @Override
  public Collection<DocumentInstance> getDocumentsAttachedToTransaction(ULID transactionLocator) {
    return apiClient.getDocumentsAttachedToTransaction(transactionLocator);
  }

  @Override
  public Preferences getPreferences(ULID transactionLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createPreferencesGetRequest(transactionLocator), Preferences.class);
  }

  @SuppressWarnings("unchecked")
  <T extends CustomerObject> Class<T> getClassFromConfig(String type) {
    return (Class<T>)
        apiClient
            .factory()
            .getDeploymentConfig()
            .getObjectClass(type)
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "There is no object class registered for type=" + type));
  }

  @Override
  public <T extends CustomerObject> T getQuoteStaticData(ULID locator) {
    return apiClient.executeAPIRequest(
        apiClient.createQuoteGetRequest(locator),
        is -> {
          QuoteAPIResponse response =
              apiClient.factory().getObjectMapper().readValue(is, QuoteAPIResponse.class);
          String type =
              CustomerDataHolder.toDataModelName(response.quote().productName())
                  + StaticDataContainer.QUOTE_STATIC_DATA;
          return apiClient.factory().readObject(response.staticData(), getClassFromConfig(type));
        });
  }

  @Override
  public <T extends CustomerObject> T getPolicyStaticData(ULID locator) {
    return apiClient.executeAPIRequest(
        apiClient.createPolicyGetRequest(locator),
        is -> {
          PolicyAPIResponse response =
              apiClient.factory().getObjectMapper().readValue(is, PolicyAPIResponse.class);
          String type =
              CustomerDataHolder.toDataModelName(response.policy().productName())
                  + StaticDataContainer.POLICY_STATIC_DATA;
          return apiClient.factory().readObject(response.staticData(), getClassFromConfig(type));
        });
  }

  @Override
  public Invoice getInvoice(ULID invoiceLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createBillingGetBaseRequest("invoices/" + invoiceLocator).build(), Invoice.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getPayment(ULID paymentLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createPaymentGetRequest(paymentLocator),
        inputStream ->
            (T) apiClient.factory().getObjectMapper().readValue(inputStream, PaymentObject.class));
  }

  @Override
  public Collection<DelinquencyEvent> getDelinquencyEvents(
      ULID delinquencyLocator, int offset, int count) {
    return apiClient.executeAPIRequest(
        apiClient
            .createBillingGetBaseRequest(
                "delinquencies/"
                    + delinquencyLocator
                    + "/events/list?offset="
                    + offset
                    + "&count="
                    + count)
            .build(),
        new TypeReference<>() {});
  }

  @Override
  public InvoiceDetails getInvoiceDetails(ULID invoiceLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createBillingGetBaseRequest("invoices/" + invoiceLocator + "/details").build(),
        InvoiceDetails.class);
  }

  @Override
  public Installment getInstallment(ULID installmentLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createBillingGetBaseRequest("installments/" + installmentLocator).build(),
        Installment.class);
  }

  @Override
  public InstallmentLattice getInstallmentLattice(ULID installmentLatticeLocator) {
    return apiClient.executeAPIRequest(
        apiClient
            .createBillingGetBaseRequest("installmentLattices/" + installmentLatticeLocator)
            .build(),
        InstallmentLattice.class);
  }

  @Override
  public Task getTask(ULID taskLocator) {
    return apiClient.executeAPIRequest(apiClient.createTaskGetRequest(taskLocator), Task.class);
  }

  @Override
  public UserAssociation getUserAssociation(ULID userAssociationLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createUserAssociationGetRequest(userAssociationLocator), UserAssociation.class);
  }

  @Override
  public Collection<FnolLoss> getFnolLosses(ULID fnolLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createClaimsBaseRequest("fnol/" + fnolLocator + "/losses").build(),
        new TypeReference<>() {});
  }

  @Override
  public Collection<ULID> getFnolClaims(ULID fnolLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createClaimsBaseRequest("fnol/" + fnolLocator + "/claims").build(),
        new TypeReference<>() {});
  }

  @Override
  public Collection<ContactRoles> getFnolContacts(ULID fnolLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createClaimsBaseRequest("fnol/" + fnolLocator + "/contacts").build(),
        new TypeReference<>() {});
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.Contact<?>> T getContact(ULID locator) {
    return apiClient.executeAPIRequest(
        apiClient.createContactBaseRequest("contacts/lookup/" + locator).build(),
        new TypeReference<>() {});
  }

  @Override
  public StreamingEntity<SubsegmentSummary> getTermSubsegmentSummaries(ULID termLocator) {
    ObjectMapper mapper = apiClient.factory().getObjectMapper();
    Map<String, Object> response =
        apiClient.executeAPIRequest(
            apiClient.createTermsBaseRequest(termLocator + "/summary").build(),
            new TypeReference<>() {});
    Collection<SubsegmentSummary> summaries =
        ((Collection<?>) response.getOrDefault("subsegments", List.of()))
            .stream().map(e -> mapper.convertValue(e, SubsegmentSummary.class)).toList();
    return new StreamingEntity<>() {

      @Override
      public void close() {
        // nothing
      }

      @Override
      public Iterator<SubsegmentSummary> iterator() {
        return summaries.iterator();
      }

      @Override
      public Stream<SubsegmentSummary> stream() {
        return summaries.stream();
      }
    };
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.Producer<?>> T getProducer(
      ULID producerLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createProducerGetRequest(producerLocator), new TypeReference<>() {});
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(
      ULID producerCodeLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createProducerCodeGetRequest(producerCodeLocator), new TypeReference<>() {});
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(
      String code) {
    return apiClient.executeAPIRequest(
        apiClient.createProducerCodeGetRequest(code), new TypeReference<>() {});
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.ProducerLicense<?>> T getProducerLicense(
      ULID producerLicenseLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createProducerLicenseGetRequest(producerLicenseLocator),
        new TypeReference<>() {});
  }

  @Override
  public <T extends com.socotra.coremodel.interfaces.ProducerAppointment<?>>
      T getProducerAppointment(ULID producerAppointmentLocator) {
    return apiClient.executeAPIRequest(
        apiClient.createProducerAppointmentGetRequest(producerAppointmentLocator),
        new TypeReference<>() {});
  }
}
