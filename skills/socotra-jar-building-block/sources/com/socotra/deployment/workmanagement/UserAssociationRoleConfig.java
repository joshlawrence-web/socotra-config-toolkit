package com.socotra.deployment.workmanagement;

import com.socotra.coremodel.TaskReferenceType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Builder
public record UserAssociationRoleConfig(
    Set<TaskReferenceType> appliesTo,
    Boolean exclusive,
    Collection<QualificationRequirement> qualification) {
  public UserAssociationRoleConfig {
    if (exclusive == null) {
      exclusive = false;
    }
    if (qualification == null) {
      qualification = List.of();
    }
  }
}
