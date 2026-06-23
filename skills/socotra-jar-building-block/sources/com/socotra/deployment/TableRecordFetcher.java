package com.socotra.deployment;

import com.socotra.coremodel.CustomerObject;
import com.socotra.coremodel.TableMetadata;
import java.util.Optional;

public interface TableRecordFetcher<T extends TableMetadata & CustomerObject> {
  Optional<T> getRecord(byte[] key);
}
