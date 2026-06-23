package com.socotra.deployment.producermanagement;

import com.socotra.coremodel.CustomerObject;
import com.socotra.coremodel.UnderwritingFlagCore;
import com.socotra.coremodel.UnderwritingLevel;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;

@Builder
public record ProducerManagementConfig(
    Map<String, Class<? extends CustomerObject>> producers,
    Map<String, Class<? extends CustomerObject>> producerCodes,
    Map<String, Class<? extends CustomerObject>> producerLicenses,
    Map<String, Class<? extends CustomerObject>> producerAppointments,
    UnderwritingFlagCore underwritingFlag) {
  public ProducerManagementConfig {
    producers = Objects.requireNonNullElseGet(producers, Map::of);
    producerCodes = Objects.requireNonNullElseGet(producerCodes, Map::of);
    producerLicenses = Objects.requireNonNullElseGet(producerLicenses, Map::of);
    producerAppointments = Objects.requireNonNullElseGet(producerAppointments, Map::of);
    underwritingFlag =
        Objects.requireNonNullElse(
            underwritingFlag, UnderwritingFlagCore.builder().level(UnderwritingLevel.none).build());
  }
}
