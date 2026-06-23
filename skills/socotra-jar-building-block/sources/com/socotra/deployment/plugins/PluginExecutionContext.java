package com.socotra.deployment.plugins;

import com.socotra.deployment.DeploymentFactory;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class PluginExecutionContext {
  private static PluginExecutionContext instance;

  protected PluginExecutionContext() {
    if (instance != null) {
      throw new IllegalStateException("PluginExecutionContext has been already initialized");
    } else {
      instance = this;
    }
  }

  public static PluginExecutionContext get() {
    return instance;
  }

  public abstract String getRequestId();

  public abstract Optional<UUID> getTenantLocator();

  public abstract Optional<String> getBusinessAccount();

  public abstract Optional<UUID> getUserLocator();

  public abstract Set<String> getUserRoles();

  public abstract Optional<AutomationPluginContextData> getAutomationPluginContext();

  public abstract Optional<DeploymentFactory> getDeploymentFactory();
}
