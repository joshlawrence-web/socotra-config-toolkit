package com.socotra.developer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.socotra.coremodel.*;
import com.socotra.deployment.DataFetcherFactory;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class APIClient {

  public static final String TENANT_LOCATOR_ENV = "SOCOTRA_EC_TENANT_LOCATOR";
  public static final String PERSONAL_ACCESS_TOKEN = "SOCOTRA_EC_PERSONAL_TOKEN";
  public static final String API_URL_ENV = "SOCOTRA_EC_API_URL";

  private static final APIClient INSTANCE = new APIClient();

  private final String apiUrl;
  private final UUID tenantLocator;
  private final String personalAccessToken;

  private HttpClient httpClient;
  private DeploymentFactory factory;

  private final APIDataFetcher apiDataFetcher;
  private final APIResourceSelector.APIResourceSelectorFactory resourceSelectorFactory;

  private APIClient() {
    this.apiUrl = System.getenv(API_URL_ENV);
    if (System.getenv(TENANT_LOCATOR_ENV) != null) {
      this.tenantLocator = UUID.fromString(System.getenv(TENANT_LOCATOR_ENV));
    } else {
      this.tenantLocator = null;
    }
    this.personalAccessToken = System.getenv(PERSONAL_ACCESS_TOKEN);

    Objects.requireNonNull(apiUrl);
    Objects.requireNonNull(tenantLocator);
    initClient();
    this.apiDataFetcher = new APIDataFetcher(this);
    this.resourceSelectorFactory = new APIResourceSelector.APIResourceSelectorFactory(this);
  }

  private void initClient() {
    try {
      this.httpClient = HttpClient.newBuilder().build();
      this.factory =
          (DeploymentFactory)
              ClassLoader.getSystemClassLoader()
                  .loadClass(DeploymentFactory.DEPLOYMENT_FACTORY_CLASS)
                  .getDeclaredConstructor()
                  .newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public HttpRequest.Builder createPolicyGetBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/policy/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createResourceGetBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/resource/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createAuxDataGetBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/auxdata/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createBillingGetBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/billing/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createProducersGetBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/producers/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createWorkManagementBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/work-management/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createClaimsBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/claim/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createContactBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/contact/"
                    + tenantLocator
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest.Builder createTermsBaseRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(
            URI.create(
                this.apiUrl
                    + "/policy/"
                    + tenantLocator
                    + "/terms"
                    + (path.startsWith("/") ? path : "/" + path)))
        .setHeader("Authorization", "Bearer " + personalAccessToken)
        .setHeader("Content-type", "application/json")
        .GET();
  }

  public HttpRequest createAccountGetRequest(ULID accountLocator) {
    return createPolicyGetBaseRequest("accounts/" + accountLocator).build();
  }

  public Collection<DocumentInstance> getQuoteDocuments(ULID quoteLocator) {
    return getDocuments("quote", quoteLocator);
  }

  public Collection<DocumentInstance> getSegmentDocuments(ULID segmentLocator) {
    return getDocuments("segment", segmentLocator);
  }

  public Collection<DocumentInstance> getDocumentsAttachedToTransaction(ULID transactionLocator) {
    return getDocuments("transaction", transactionLocator);
  }

  private Collection<DocumentInstance> getDocuments(String referenceObjectType, ULID locator) {
    return executeAPIRequest(
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    this.apiUrl
                        + "/document/"
                        + tenantLocator
                        + "/documents/"
                        + referenceObjectType
                        + "/"
                        + locator
                        + "/list"))
            .setHeader("Authorization", "Bearer " + personalAccessToken)
            .GET()
            .build(),
        new TypeReference<>() {});
  }

  public UnderwritingFlags getQuoteUnderwritingFlags(ULID quoteLocator) {
    return null;
  }

  public HttpRequest createQuickQuoteGetRequest(ULID quickQuoteLocator) {
    return createPolicyGetBaseRequest("quickquotes/" + quickQuoteLocator).build();
  }

  public HttpRequest createQuoteGetRequest(ULID quoteLocator) {
    return createPolicyGetBaseRequest("quotes/" + quoteLocator).build();
  }

  public HttpRequest createQuoteGroupGetRequest(ULID quoteGroupLocator) {
    return createPolicyGetBaseRequest("quotes/groups/" + quoteGroupLocator).build();
  }

  public HttpRequest createQuotePricingGetRequest(ULID quoteLocator) {
    return createPolicyGetBaseRequest("quotes/" + quoteLocator + "/price").build();
  }

  public HttpRequest createQuoteUnderwritingFlagGetRequest(ULID quoteLocator) {
    return createPolicyGetBaseRequest("quotes/" + quoteLocator + "/underwritingFlags").build();
  }

  public HttpRequest createTransactionGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest("transactions/" + transactionLocator).build();
  }

  public HttpRequest createTransactionUnderwritingFlagGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest("transactions/" + transactionLocator + "/underwritingFlags")
        .build();
  }

  public HttpRequest createUnderwritingFlagGetRequest(ULID underwritingFlagLocator) {
    return createPolicyGetBaseRequest("underwritingFlags/" + underwritingFlagLocator).build();
  }

  public HttpRequest createTransactionPriceGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest("transactions/" + transactionLocator + "/price").build();
  }

  public HttpRequest createPreferencesGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest("transactions/" + transactionLocator + "/preferences")
        .build();
  }

  public HttpRequest createTransactionAffectedListGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest(
            "transactions/" + transactionLocator + "/affectedTransactions/list")
        .build();
  }

  public HttpRequest createTransactionSegmentGetRequest(ULID transactionLocator) {
    return createPolicyGetBaseRequest("transactions/" + transactionLocator + "/segment").build();
  }

  public HttpRequest createSegmentGetRequest(ULID segmentLocator) {
    return createPolicyGetBaseRequest("segments/" + segmentLocator).build();
  }

  public HttpRequest createPolicyGetRequest(ULID policyLocator) {
    return createPolicyGetBaseRequest("policies/" + policyLocator).build();
  }

  public HttpRequest createPaymentGetRequest(ULID paymentLocator) {
    return createBillingGetBaseRequest("payments/" + paymentLocator).build();
  }

  public HttpRequest createProducerGetRequest(ULID producerLocator) {
    return createProducersGetBaseRequest("producers/" + producerLocator).build();
  }

  public HttpRequest createProducerCodeGetRequest(ULID producerCodeLocator) {
    return createProducersGetBaseRequest("producerCodes/" + producerCodeLocator).build();
  }

  public HttpRequest createProducerCodeGetRequest(String code) {
    return createProducersGetBaseRequest("producerCodes/" + code).build();
  }

  public HttpRequest createProducerLicenseGetRequest(ULID producerLicenseLocator) {
    return createProducersGetBaseRequest("licenses/" + producerLicenseLocator).build();
  }

  public HttpRequest createProducerAppointmentGetRequest(ULID producerAppointmentLocator) {
    return createProducersGetBaseRequest("appointments/" + producerAppointmentLocator).build();
  }

  public HttpRequest createTaskGetRequest(ULID taskLocator) {
    return createWorkManagementBaseRequest("tasks/" + taskLocator).build();
  }

  public HttpRequest createUserAssociationGetRequest(ULID userAssociationLocator) {
    return createWorkManagementBaseRequest("userAssociations/" + userAssociationLocator).build();
  }

  public HttpRequest createFnolGetRequest(ULID fnolLocator) {
    return createClaimsBaseRequest("fnols/" + fnolLocator).build();
  }

  private <T> T execute(ThrowableSupplier<T> provider) {
    try {
      return provider.get();
    } catch (APIError e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T executeAPIRequest(HttpRequest request, Class<T> responseClass) {
    return executeAPIRequest(request, is -> factory.getObjectMapper().readValue(is, responseClass));
  }

  public <T> T executeAPIRequest(HttpRequest request, TypeReference<T> typeReference) {
    return executeAPIRequest(request, is -> factory.getObjectMapper().readValue(is, typeReference));
  }

  public <T> T executeAPIRequest(HttpRequest request, ThrowableFunction<InputStream, T> processor) {
    return execute(
        () -> {
          HttpResponse<InputStream> response =
              httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
          try (InputStream is = response.body()) {
            if (response.statusCode() != 200) {
              throw new APIError(response.statusCode(), new String(is.readAllBytes()));
            }
            return processor.apply(is);
          }
        });
  }

  public DeploymentFactory factory() {
    return factory;
  }

  @FunctionalInterface
  public interface ThrowableSupplier<T> {

    T get() throws Exception;
  }

  @FunctionalInterface
  public interface ThrowableFunction<T, R> {

    R apply(T var1) throws Exception;
  }

  public static void initialize() {
    DataFetcherFactory.supplier(() -> INSTANCE.apiDataFetcher);
  }

  public static class APIError extends RuntimeException {

    private int statusCode;

    public APIError(int statusCode, String message) {
      super(message);
      this.statusCode = statusCode;
    }

    public int getStatusCode() {
      return statusCode;
    }

    @Override
    public String toString() {
      return "Bad response: " + statusCode + " - " + getMessage();
    }
  }
}
