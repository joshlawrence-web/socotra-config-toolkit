package com.socotra.deployment.customer;

import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

public final class CustomerDeployment extends AbstractDeploymentFactory {

  private static final DeploymentConfig deploymentConfig;
  private static final ULID staticVersionLocator = ULID.from("01KSMHW6GQ441ZV23AR9FBMB5G");

  @Override
  public DeploymentConfig getDeploymentConfig() {
    return deploymentConfig;
  }

  public static ULID staticVersionLocator() {
    return staticVersionLocator;
  }

  @Override
  public ULID getStaticVersionLocator() {
    return staticVersionLocator;
  }

  static {
    deploymentConfig = new CustomerConfig(new CustomerDeployment());
  }
}