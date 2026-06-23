package com.socotra.deployment.schedules;

import com.socotra.coremodel.ChargeType;
import com.socotra.coremodel.RatingItem;
import com.socotra.platform.tools.ULID;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RatingRegistryImpl implements RatingRegistry {
  private final Map<ChargeType, RatingItem.RatingItemBuilder> ratingBuilders;
  private final RatingItemsWriter ratingItemsWriter;
  private final ULID elementLocator;

  public RatingRegistryImpl(ULID elementLocator) {
    this(elementLocator, null);
  }

  public RatingRegistryImpl(ULID elementLocator, RatingItemsWriter ratingItemsWriter) {
    ratingBuilders = new ConcurrentHashMap<>();
    this.elementLocator = elementLocator;
    this.ratingItemsWriter = ratingItemsWriter;
  }

  @Override
  public void register(RatingItem ratingItem) {
    RatingItem.RatingItemBuilder builder =
        ratingBuilders.computeIfAbsent(
            ratingItem.chargeType(),
            k -> RatingItem.builder().chargeType(k).elementLocator(elementLocator));
    synchronized (builder) {
      ratingItem
          .amount()
          .map(amount -> builder.amount().map(amount::add).orElse(amount))
          .ifPresent(builder::amount);
      ratingItem
          .rate()
          .map(rate -> builder.rate().map(rate::add).orElse(rate))
          .ifPresent(builder::rate);
    }
    if (ratingItemsWriter != null) {
      ratingItemsWriter.write(ratingItem);
    }
  }

  @Override
  public Collection<RatingItem> aggregate() {
    return ratingBuilders.values().stream()
        .map(RatingItem.RatingItemBuilder::build)
        .collect(Collectors.toList());
  }
}
