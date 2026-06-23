package com.socotra.developer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.util.*;

public final class APIResourceSelector implements ResourceSelector {
  private final APIClient apiClient;
  private final Object object;

  private APIResourceSelector(APIClient apiClient, Object o) {
    this.apiClient = apiClient;
    this.object = o;
  }

  @Override
  public <T extends TableMetadata & CustomerObject> TableRecordFetcher<T> getTable(
      Class<T> tableType) {
    return new APITableRecordFetcher<>(apiClient, tableType, object);
  }

  @Override
  public <T extends RangeTableMetadata & CustomerObject> RangeTableRecordFetcher<T> getRangeTable(
      Class<T> tableType) {
    return new APIRangeTableRecordFetcher<>(apiClient, tableType, object);
  }

  @Override
  public ConstraintsFetcher getConstraints(Class<? extends ConstraintTableMetadata> tableType) {
    return new APIConstraintFetcher(apiClient, tableType, object);
  }

  @Override
  public <T> Optional<T> getSecret(Class<T> secretType) {
    try {
      String staticName = (String) secretType.getMethod("getStaticName").invoke(null);
      Optional<String> jurisdiction = ResourceSelector.jurisdiction(object);
      Map<String, Object> response =
          apiClient.executeAPIRequest(
              apiClient
                  .createResourceGetBaseRequest(
                      "/secrets/"
                          + staticName
                          + "?byStaticName=true"
                          + jurisdiction.map(j -> "&jurisdiction=" + j).orElse(""))
                  .build(),
              new TypeReference<>() {});
      return Optional.ofNullable(
          apiClient.factory().getObjectMapper().convertValue(response.get("secret"), secretType));
    } catch (APIClient.APIError e) {
      if (e.getStatusCode() == 404) {
        return Optional.empty();
      }
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch the secret " + secretType.getSimpleName(), e);
    }
  }

  public static class APIResourceSelectorFactory extends ResourceSelectorFactory {
    private final APIClient apiClient;

    public APIResourceSelectorFactory(APIClient apiClient) {
      this.apiClient = apiClient;
    }

    @Override
    public ResourceSelector getSelector(Object forObject) {
      return new APIResourceSelector(apiClient, forObject);
    }
  }

  public static class APIConstraintFetcher implements ConstraintsFetcher {
    private final APIClient apiClient;
    private final Class<? extends TableMetadata> tableClass;
    private final Object object;

    public APIConstraintFetcher(
        APIClient apiClient, Class<? extends TableMetadata> tableClass, Object object) {
      this.apiClient = apiClient;
      this.tableClass = tableClass;
      this.object = object;
    }

    @Override
    public Collection<String> get(byte[] key) {
      Map.Entry<String, SelectionTimeBasis> entry =
          ResourceSelector.extractStaticNameAndBasis(tableClass);
      ObjectMapper objectMapper = apiClient.factory().getObjectMapper();
      try {
        String json =
            objectMapper.writeValueAsString(
                Map.of(
                    "date",
                    ResourceSelector.selectionTime(entry.getValue(), object),
                    "jurisdiction",
                    ResourceSelector.jurisdiction(object),
                    "hash",
                    new String(Base64.getEncoder().encode(key))));

        return apiClient.executeAPIRequest(
            apiClient
                .createResourceGetBaseRequest("/constraints/" + entry.getKey() + "/record")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            new TypeReference<>() {});
      } catch (APIClient.APIError e) {
        if (e.getStatusCode() == 404) {
          return List.of();
        }
        throw e;
      } catch (Exception e) {
        throw new RuntimeException("Failed to retrieve table record", e);
      }
    }
  }

  public static class APITableRecordFetcher<T extends TableMetadata & CustomerObject>
      implements TableRecordFetcher<T> {
    private final APIClient apiClient;
    private final Class<T> tableClass;
    private final Object object;

    private APITableRecordFetcher(APIClient apiClient, Class<T> tableClass, Object object) {
      this.apiClient = apiClient;
      this.tableClass = tableClass;
      this.object = object;
    }

    @Override
    public Optional<T> getRecord(byte[] key) {
      Map.Entry<String, SelectionTimeBasis> entry =
          ResourceSelector.extractStaticNameAndBasis(tableClass);
      ObjectMapper objectMapper = apiClient.factory().getObjectMapper();
      try {
        String json =
            objectMapper.writeValueAsString(
                Map.of(
                    "date",
                    ResourceSelector.selectionTime(entry.getValue(), object),
                    "jurisdiction",
                    ResourceSelector.jurisdiction(object),
                    "hash",
                    new String(Base64.getEncoder().encode(key))));

        Map<String, Object> result =
            apiClient.executeAPIRequest(
                apiClient
                    .createResourceGetBaseRequest("/tables/" + entry.getKey() + "/record")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build(),
                new TypeReference<>() {});
        return Optional.ofNullable(objectMapper.convertValue(result.get("value"), tableClass));
      } catch (APIClient.APIError e) {
        if (e.getStatusCode() == 404) {
          return Optional.empty();
        }
        throw e;
      } catch (Exception e) {
        throw new RuntimeException("Failed to retrieve table record", e);
      }
    }
  }

  public static class APIRangeTableRecordFetcher<T extends RangeTableMetadata & CustomerObject>
      implements RangeTableRecordFetcher<T> {
    private final APIClient apiClient;
    private final Class<T> tableClass;
    private final Object object;

    private APIRangeTableRecordFetcher(APIClient apiClient, Class<T> tableClass, Object object) {
      this.apiClient = apiClient;
      this.tableClass = tableClass;
      this.object = object;
    }

    @Override
    public Optional<T> getRecord(byte[] key, BigDecimal boundValue) {
      Map.Entry<String, SelectionTimeBasis> entry =
          ResourceSelector.extractStaticNameAndBasis(tableClass);
      return fetch(
          "/rangeTables/" + entry.getKey() + "/record",
          Map.of(
              "date",
              ResourceSelector.selectionTime(entry.getValue(), object),
              "jurisdiction",
              ResourceSelector.jurisdiction(object),
              "hash",
              new String(Base64.getEncoder().encode(key)),
              "boundValue",
              boundValue));
    }

    @Override
    public Optional<T> getLowerAdjacentRecord(byte[] key, BigDecimal boundValue) {
      Map.Entry<String, SelectionTimeBasis> entry =
          ResourceSelector.extractStaticNameAndBasis(tableClass);
      return fetch(
          "/rangeTables/" + entry.getKey() + "/lowerRecord",
          Map.of(
              "date",
              ResourceSelector.selectionTime(entry.getValue(), object),
              "jurisdiction",
              ResourceSelector.jurisdiction(object),
              "hash",
              new String(Base64.getEncoder().encode(key)),
              "boundValue",
              boundValue));
    }

    @Override
    public Optional<T> getUpperAdjacentRecord(byte[] key, BigDecimal boundValue) {
      Map.Entry<String, SelectionTimeBasis> entry =
          ResourceSelector.extractStaticNameAndBasis(tableClass);
      return fetch(
          "/rangeTables/" + entry.getKey() + "/upperRecord",
          Map.of(
              "date",
              ResourceSelector.selectionTime(entry.getValue(), object),
              "jurisdiction",
              ResourceSelector.jurisdiction(object),
              "hash",
              new String(Base64.getEncoder().encode(key)),
              "boundValue",
              boundValue));
    }

    private Optional<T> fetch(String path, Map<String, Object> request) {
      ObjectMapper objectMapper = apiClient.factory().getObjectMapper();
      try {
        Map<String, Object> result =
            apiClient.executeAPIRequest(
                apiClient
                    .createResourceGetBaseRequest(path)
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(request)))
                    .build(),
                new TypeReference<>() {});

        return Optional.ofNullable(objectMapper.convertValue(result.get("value"), tableClass));
      } catch (APIClient.APIError e) {
        if (e.getStatusCode() == 404) {
          return Optional.empty();
        }
        throw e;
      } catch (Exception e) {
        throw new RuntimeException("Failed to retrieve table record", e);
      }
    }
  }
}
