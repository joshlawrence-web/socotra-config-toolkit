package com.socotra.coremodel;

import com.socotra.deployment.TimeService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum EventTimeAlignment {
  weekStart,
  monthStart,
  yearStart;

  public Instant align(Instant anchorTime, TimeService timeService) {
    ChronoUnit chronoUnit =
        switch (this) {
          case weekStart -> ChronoUnit.WEEKS;
          case monthStart -> ChronoUnit.MONTHS;
          case yearStart -> ChronoUnit.YEARS;
        };
    return timeService.alignForward(anchorTime, chronoUnit);
  }
}
