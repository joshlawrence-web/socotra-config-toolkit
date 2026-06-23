package com.socotra.coremodel;

import java.util.Map;

public interface Moratoriums {
  Map<String, MoratoriumConfig> getMoratoriumConfigs();

  Map<String, MoratoriumConfig> checkForMoratoriums(Quote quote);

  Map<String, MoratoriumConfig> checkForMoratoriums(Policy policy, Segment segment);
}
