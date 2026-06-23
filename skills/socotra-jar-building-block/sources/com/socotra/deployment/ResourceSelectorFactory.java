package com.socotra.deployment;

public abstract class ResourceSelectorFactory {
  private static ResourceSelectorFactory INSTANCE;

  protected ResourceSelectorFactory() {
    INSTANCE = this;
  }

  public abstract ResourceSelector getSelector(Object forObject);

  public static ResourceSelectorFactory getInstance() {
    if (INSTANCE == null) {
      throw new IllegalStateException("ResourceSelector is not supported");
    }
    return INSTANCE;
  }
}
