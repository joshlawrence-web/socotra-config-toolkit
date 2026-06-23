package com.socotra.deployment;

import com.socotra.coremodel.DurationBasis;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.TimeUnit;

public final class TimeService {
  public static final ZoneId UTC = ZoneId.of("UTC");

  private final ZoneId timezone;
  private final DurationBasis durationBasis;

  public TimeService(String timezone, DurationBasis durationBasis) {
    this.timezone = ZoneId.of(timezone);
    this.durationBasis = durationBasis;
  }

  public ZoneId timezone() {
    return timezone;
  }

  public Instant alignForward(Instant instant, ChronoUnit chronoUnit) {
    OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, timezone);
    OffsetDateTime updatedDateTime =
        switch (chronoUnit) {
          case YEARS ->
              offsetDateTime
                  .truncatedTo(ChronoUnit.DAYS)
                  .with(TemporalAdjusters.firstDayOfNextYear());
          case MONTHS ->
              offsetDateTime
                  .truncatedTo(ChronoUnit.DAYS)
                  .with(TemporalAdjusters.firstDayOfNextMonth());
          case WEEKS ->
              offsetDateTime
                  .truncatedTo(ChronoUnit.DAYS)
                  .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
          default -> offsetDateTime.truncatedTo(chronoUnit).plus(1, chronoUnit);
        };
    return updatedDateTime.toInstant();
  }

  public Instant tomorrowMidnight() {
    return OffsetDateTime.now(timezone).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant();
  }

  public Instant todayMidnight() {
    return OffsetDateTime.now(timezone).truncatedTo(ChronoUnit.DAYS).toInstant();
  }

  public Instant addDuration(Instant instant, int duration) {
    return addDuration(instant, (double) duration);
  }

  public Instant addDuration(Instant instant, double duration) {
    return addDuration(instant, duration, durationBasis);
  }

  public Instant addDuration(Instant instant, double duration, DurationBasis durationBasis) {
    LocalDateTime tmp = LocalDateTime.ofInstant(instant, timezone);
    tmp =
        switch (durationBasis) {
          case years -> addMonths(tmp, duration * 12, false);
          case months -> addMonths(tmp, duration, false);
          case monthsE360 -> addMonths(tmp, duration, true);
          case weeks -> addDays(tmp, duration * 7);
          case days -> addDays(tmp, duration);
          case hours -> addDays(tmp, duration / 24);
          default -> throw new IllegalArgumentException("Unsupported duration:" + durationBasis);
        };
    return tmp.atZone(timezone).toInstant();
  }

  private int getDaysInMonth(LocalDateTime date, boolean isMonths360) {
    return isMonths360 ? 30 : date.toLocalDate().lengthOfMonth();
  }

  private LocalDateTime addMonths(
      LocalDateTime localDateTime, double duration, boolean isMonths360) {
    double fraction = duration % 1;
    int months = (int) (duration - fraction);
    // We deal with full number of the months thus internal calculation does the math for us
    if (fraction == 0) {
      return roundToSeconds(localDateTime.plusMonths(months));
    }

    int day = localDateTime.getDayOfMonth();
    long timeOfDay = timeOfDayInMilliseconds(localDateTime);

    localDateTime =
        localDateTime.plusMonths(months).withHour(0).withMinute(0).withSecond(0).withNano(0);
    int daysInMonth = getDaysInMonth(localDateTime, isMonths360);
    double daysInFractionMonth = daysInMonth * fraction;

    // if the sum(localDateTime, timeOfDay, daysInFractionalMonth)
    // is less then the sum(daysInMonth, 1),
    // the addition will not overflow to the next month
    if (localDateTime.getDayOfMonth()
            + daysInFractionMonth
            + ((double) timeOfDay / TimeUnit.DAYS.toMillis(1))
        < daysInMonth + 1) {
      return addDays(localDateTime, daysInFractionMonth)
          .plusNanos(TimeUnit.MILLISECONDS.toNanos(timeOfDay));
    }

    localDateTime = localDateTime.plusMonths(1);
    daysInMonth = getDaysInMonth(localDateTime, isMonths360);
    daysInFractionMonth = daysInMonth * fraction;
    return addDays(
            localDateTime.withDayOfMonth(Math.min(day, daysInMonth)),
            -(daysInMonth - daysInFractionMonth))
        .plusNanos(TimeUnit.MILLISECONDS.toNanos(timeOfDay));
  }

  /**
   * The function checks the nanoseconds and rounds HALF_UP to next second
   *
   * @param localDateTime
   * @return
   */
  public static LocalDateTime roundToSeconds(LocalDateTime localDateTime) {
    if (TimeUnit.NANOSECONDS.toMillis(localDateTime.getNano()) > 499) {
      return localDateTime.plusSeconds(1).withNano(0);
    } else {
      return localDateTime.withNano(0);
    }
  }

  private LocalDateTime addDays(LocalDateTime localDateTime, double duration) {
    double fraction = duration % 1;
    int days = (int) (duration - fraction);
    localDateTime = localDateTime.plusDays(days);
    long extraNanos = (long) (TimeUnit.DAYS.toNanos(1) * fraction);
    return roundToSeconds(localDateTime.plusNanos(extraNanos));
  }

  @Deprecated
  // TODO - Remove the method since it will produce incorrect duration calculations for segments
  // within a single term
  public BigDecimal calculateDuration(Instant startTime, Instant endTime) {
    return calculateDuration(startTime, endTime, startTime);
  }

  public BigDecimal calculateDuration(Instant startTime, Instant endTime, Instant termStartTime) {
    LocalDateTime localStartTime = LocalDateTime.ofInstant(startTime, timezone);
    LocalDateTime localEndTime = LocalDateTime.ofInstant(endTime, timezone);
    LocalDateTime localStartTermTime = LocalDateTime.ofInstant(termStartTime, timezone);
    return switch (durationBasis) {
      case years ->
          monthsDuration(localStartTime, localEndTime, localStartTermTime)
              .setScale(15, RoundingMode.HALF_EVEN)
              .divide(BigDecimal.valueOf(12.0), RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      case months ->
          monthsDuration(localStartTime, localEndTime, localStartTermTime)
              .setScale(15, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      case monthsE360 ->
          durationFromDatesInMonths360(localStartTime, localEndTime)
              .setScale(15, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      case weeks ->
          durationFromDatesInDays(startTime, endTime)
              .setScale(15, RoundingMode.HALF_EVEN)
              .divide(BigDecimal.valueOf(7), RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      case days ->
          durationFromDatesInDays(startTime, endTime)
              .setScale(15, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      case hours ->
          durationFromDatesInDays(startTime, endTime)
              .multiply(BigDecimal.valueOf(24))
              .setScale(15, RoundingMode.HALF_EVEN)
              .stripTrailingZeros();
      default -> throw new IllegalArgumentException("Unsupported duration:" + durationBasis);
    };
  }

  private BigDecimal monthsDuration(
      LocalDateTime startTime, LocalDateTime endTime, LocalDateTime anchorTime) {
    if (startTime.equals(endTime)) {
      return BigDecimal.ZERO;
    }
    if (anchorTime.equals(startTime)) {
      return durationFromDatesInMonths(startTime, endTime);
    }
    return durationFromDatesInMonths(anchorTime, endTime)
        .subtract(durationFromDatesInMonths(anchorTime, startTime));
  }

  private BigDecimal durationFromDatesInDays(Instant startTime, Instant endTime) {
    BigDecimal diff =
        BigDecimal.valueOf(endTime.toEpochMilli() - startTime.toEpochMilli())
            .setScale(30, RoundingMode.HALF_EVEN)
            .divide(BigDecimal.valueOf(TimeUnit.DAYS.toMillis(1)), RoundingMode.HALF_EVEN);
    BigDecimal zoneDiff =
        BigDecimal.valueOf(
            ZonedDateTime.ofInstant(startTime, timezone).getOffset().getTotalSeconds()
                - ZonedDateTime.ofInstant(endTime, timezone).getOffset().getTotalSeconds());
    return diff.subtract(
        zoneDiff
            .setScale(30, RoundingMode.HALF_EVEN)
            .divide(BigDecimal.valueOf(24 * 60 * 60), RoundingMode.HALF_EVEN));
  }

  private BigDecimal durationFromDatesInMonths360(LocalDateTime startTime, LocalDateTime endTime) {
    int day1 = Math.min(startTime.getDayOfMonth(), 30);
    int day2 = Math.min(endTime.getDayOfMonth(), 30);
    return BigDecimal.valueOf(
        (endTime.getYear() - startTime.getYear()) * 12
            + (endTime.getMonthValue() - startTime.getMonthValue())
            + ((double) (day2 - day1) / 30));
  }

  private BigDecimal durationFromDatesInMonths(LocalDateTime startTime, LocalDateTime endTime) {
    long timeOfDayDiff = timeOfDayInMilliseconds(endTime) - timeOfDayInMilliseconds(startTime);
    int wholeMonths =
        (endTime.getYear() - startTime.getYear()) * 12
            + (endTime.getMonthValue() - startTime.getMonthValue());
    int daysInMonth = endTime.toLocalDate().lengthOfMonth();
    long extraMs =
        TimeUnit.DAYS.toMillis(
                (endTime.getDayOfMonth() - Math.min(daysInMonth, startTime.getDayOfMonth())))
            + timeOfDayDiff;
    long msInMonth = TimeUnit.DAYS.toMillis(daysInMonth);
    return new BigDecimal(wholeMonths + ((double) extraMs / msInMonth));
  }

  private long timeOfDayInMilliseconds(LocalDateTime dateTime) {
    return TimeUnit.NANOSECONDS.toMillis(dateTime.getNano())
        + TimeUnit.SECONDS.toMillis(dateTime.getSecond())
        + TimeUnit.MINUTES.toMillis(dateTime.getMinute())
        + TimeUnit.HOURS.toMillis(dateTime.getHour());
  }
}
