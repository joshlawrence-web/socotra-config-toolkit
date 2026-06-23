package com.socotra.deployment.workmanagement;

import com.socotra.coremodel.*;

public interface WorkManagementService {

  static WorkManagementService getInstance() {
    return WorkManagementServiceFactory.get();
  }

  TaskCreateResponse createTask(TaskCreateRequest request);
}
