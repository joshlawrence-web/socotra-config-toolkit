package com.socotra.deployment;

import com.socotra.coremodel.AuxDataSetCreateRequest;

public interface AuxDataService {

  static AuxDataService getInstance() {
    return AuxDataServiceFactory.get();
  }

  void setAuxData(String locator, AuxDataSetCreateRequest request);

  void deleteAuxData(String locator, String key);
}
