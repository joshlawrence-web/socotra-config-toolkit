package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.platform.tools.ULID;

import java.util.*;
import java.math.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

@Plugin(type = PluginType.workplanSelection)
public interface WorkplanSelectionPlugin {

  default WorkplanSelectionResponse selectWorkplans(WorkplanSelectionRequest request) {
    return WorkplanSelectionResponse.builder()
        .workplansToExecute(request.workplansSelection().workplans())
        .build();
  }

  public static final class WorkplanSelectionPluginStub implements WorkplanSelectionPlugin {
  }

  public static record WorkplanSelectionRequest(
        WorkplanSelection workplansSelection
  ) implements PluginRequest {
    public Collection<?> structures() { return List.of(workplansSelection()); }
  }
}