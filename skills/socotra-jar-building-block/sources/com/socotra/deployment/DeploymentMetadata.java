package com.socotra.deployment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.*;
import lombok.Builder;
import lombok.NonNull;

/**
 * DeploymentMetadata holds information about Customer's deployments
 *
 * @param version1
 */
@Builder
public record DeploymentMetadata(
    @NonNull ULID version1,
    @Deprecated(since = "2026-02-24", forRemoval = true) ULID version2,
    Optional<ULID> staticVersionLocator,
    Map<String, String> implementedPlugins,
    Map<String, String> implementedAutomationPlugins,
    Optional<Instant> latestOverwriteAt,
    Optional<UUID> latestOverwriteBy) {
  public static final String METADATA_NAME = "metadata.json";

  public DeploymentMetadata {
    if (implementedPlugins == null) {
      implementedPlugins = Collections.emptyMap();
    } else {
      implementedPlugins = Map.copyOf(implementedPlugins);
    }

    if (implementedAutomationPlugins == null) {
      implementedAutomationPlugins = Collections.emptyMap();
    } else {
      implementedAutomationPlugins = Map.copyOf(implementedAutomationPlugins);
    }
  }

  public Map<ULID, Map<String, String>> plugins() {
    return Map.of(version1, implementedPlugins);
  }

  public Map<ULID, Map<String, String>> automationPlugins() {
    return Map.of(version1, implementedAutomationPlugins);
  }

  // Backward compatibility constructor to support old metadata format.
  public DeploymentMetadata(
      @NonNull ULID version1,
      ULID version2,
      Map<ULID, Map<String, String>> plugins,
      Map<ULID, Map<String, String>> automationPlugins) {
    this(
        version2 == null ? version1 : version1.getTime() < version2.getTime() ? version2 : version1,
        null,
        Optional.empty(),
        plugins == null
            ? null
            : plugins.get(
                version2 == null
                    ? version1
                    : version1.getTime() < version2.getTime() ? version2 : version1),
        automationPlugins == null
            ? null
            : automationPlugins.get(
                version2 == null
                    ? version1
                    : version1.getTime() < version2.getTime() ? version2 : version1),
        Optional.empty(),
        Optional.empty());
  }

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public ULID getLatestVersion() {
    if (version2 == null) {
      return version1;
    }
    return version1.getTime() < version2.getTime() ? version2 : version1;
  }

  public static class DeploymentMetadataBuilder {

    private DeploymentMetadataBuilder() {
      this.implementedPlugins = new HashMap<>();
      this.implementedAutomationPlugins = new HashMap<>();
    }

    public DeploymentMetadataBuilder plugins(ULID version, Map<String, String> plugins) {
      if (version != null && plugins != null) {
        this.implementedPlugins.putAll(plugins);
      }
      return this;
    }

    public DeploymentMetadataBuilder automationPlugins(
        ULID version, Map<String, String> automationPlugins) {
      if (version != null && automationPlugins != null) {
        this.implementedAutomationPlugins.putAll(automationPlugins);
      }
      return this;
    }
  }

  @Deprecated(since = "2026-02-24", forRemoval = true)
  public static Iterable<String> getMetadataPath(String tenantLocator) {
    return List.of(tenantLocator, METADATA_NAME);
  }

  public static Iterable<String> getMetadataPath(UUID tenantLocator, ULID versionLocator) {
    return List.of(tenantLocator.toString(), versionLocator.toString(), METADATA_NAME);
  }
}
