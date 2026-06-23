package com.socotra.deployment;

import java.util.Collection;

public interface ConstraintsFetcher {
  Collection<String> get(byte[] key);
}
