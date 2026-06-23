package com.socotra.deployment.workmanagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Map;
import lombok.Builder;

@Builder
public record WorkManagementConfig(
    Map<String, Map<String, Integer>> qualifications,
    Map<String, TasksConfig> tasks,
    Map<String, UserAssociationRoleConfig> userAssociationRoles) {

  @JsonIgnore
  public boolean verifyQualification(
      Collection<QualificationRequirement> qualificationRequirements,
      Map<String, String> providedQualification) {
    return qualificationRequirements.isEmpty()
        || qualificationRequirements.stream()
            .filter(q -> providedQualification.containsKey(q.category()))
            .map(q -> Map.entry(q, providedQualification.get(q.category())))
            .filter(e -> qualifications().get(e.getKey().category()).containsKey(e.getValue()))
            .anyMatch(
                e ->
                    e.getKey()
                        .validator()
                        .test(
                            qualifications().get(e.getKey().category()).get(e.getKey().level()),
                            qualifications().get(e.getKey().category()).get(e.getValue())));
  }
}
