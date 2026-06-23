package com.socotra.deployment.customer;

import com.socotra.deployment.*;

public final class CustomerDeployment extends AbstractDeploymentFactory {

  private static final DeploymentConfig deploymentConfig;

  @Override
  public DeploymentConfig getDeploymentConfig() {
    return deploymentConfig;
  }

  static {
    deploymentConfig = new CustomerConfig(new CustomerDeployment());
  }
}