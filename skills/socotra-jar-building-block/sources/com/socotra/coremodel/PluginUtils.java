package com.socotra.coremodel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PluginUtils {

  public static Integer detectPluginVersion(Class<?> pluginClass) {
    for (Class<?> i : pluginClass.getInterfaces()) {
      if (i.getDeclaredAnnotation(Plugin.class) != null) {
        try {
          Integer version = (Integer) i.getDeclaredField("VERSION").get(null);
          log.info("{} has version={}", i.getSimpleName(), version);
          return version;
        } catch (NoSuchFieldException e) {
          log.info(
              "VERSION field is not present on {}: {}", i.getSimpleName(), i.getDeclaredFields());
          return 0;
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Failed to detect plugin version", e);
        }
      }
    }
    throw new RuntimeException("Plugin annotation is not found");
  }
}
