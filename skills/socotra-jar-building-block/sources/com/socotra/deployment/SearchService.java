package com.socotra.deployment;

import com.socotra.coremodel.*;

public interface SearchService {
  static SearchService getInstance() {
    return SearchServiceFactory.get();
  }

  SearchConfigurationResponse getSearchConfiguration(String deployedVersion);
}
