package com.socotra.deployment.schedules;

import com.socotra.coremodel.RatingItem;

public interface RatingItemsWriter extends AutoCloseable {
  void write(RatingItem ratingItem);
}
