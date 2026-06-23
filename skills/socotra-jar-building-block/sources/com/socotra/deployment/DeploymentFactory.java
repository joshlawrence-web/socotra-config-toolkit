package com.socotra.deployment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socotra.coremodel.CustomerObject;
import com.socotra.platform.tools.ULID;
import java.util.Map;

/**
 * DeploymentFactory is loaded by DeploymentsRepository when a service needs to access to deployed
 * Customer configuration
 */
public interface DeploymentFactory {
  String CONFIG_BUNDLE_NAME = "customer-config.jar";
  String PLUGINS_BUNDLE_NAME = "customer-plugins.jar";
  String PLUGINS_INFO_NAME = "plugins-info.json";
  String AUTOMATION_PLUGINS_INFO_NAME = "automation-plugins-info.json";
  String AUTOMATION_PLUGINS_FILE_NAME_PREFIX = "automation-";

  String DEPLOYMENT_PACKAGE_NAME = "com.socotra.deployment.customer";
  String DEPLOYMENT_FACTORY_CLASS = "com.socotra.deployment.customer.CustomerDeployment";

  String MORATORIUMS_CLASS_NAME = "com.socotra.deployment.customer.MoratoriumsImpl";
  String MORATORIUMS_BUNDLE_NAME = "moratoriums.jar";

  ULID getStaticVersionLocator();

  ObjectMapper getObjectMapper();

  DeploymentConfig getDeploymentConfig();

  default <T> T readObject(Map<?, ?> map, Class<T> clazz) {
    return getObjectMapper().convertValue(map, clazz);
  }

  default Map<String, Object> writeObject(CustomerObject obj) {
    return getObjectMapper().convertValue(obj, new TypeReference<>() {});
  }
}
