package com.socotra.deployment.schedules;

import com.socotra.coremodel.RatingItem;
import java.util.Collection;

public interface RatingRegistry {
  void register(RatingItem ratingItem);

  Collection<RatingItem> aggregate();
}
