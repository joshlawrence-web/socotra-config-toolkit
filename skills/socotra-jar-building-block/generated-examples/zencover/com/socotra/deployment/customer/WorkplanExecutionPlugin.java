package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

@Plugin(type = PluginType.workplanExecution)
public interface WorkplanExecutionPlugin {

  default WorkplanExecutionResponse decorateWorkplanExecution(WorkplanExecutionRequest request) {
    return WorkplanExecutionResponse.builder()
        .tasks(request.execution().tasks())
        .associations(request.execution().associations())
        .build();
  }

  public static final class WorkplanExecutionPluginStub implements WorkplanExecutionPlugin {
  }

  public static record WorkplanExecutionRequest(
        WorkplanExecution execution
  ) implements PluginRequest {
    public Collection<?> structures() { return List.of(execution()); }
  }
}