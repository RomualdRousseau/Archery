package com.github.romualdrousseau.archery.commons.time;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public class DateUtils {

    public static final String DEFAULT_JAVA_DATE_FORMAT = "yyyy-MM-dd";

    public static final ThreadLocal<SimpleDateFormat> DEFAULT_DATE_FORMATER = new ThreadLocal<>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DateUtils.DEFAULT_JAVA_DATE_FORMAT);
        }
    };

    public static final ThreadLocal<SimpleDateFormat> DEFAULT_DATE_PARSER = new ThreadLocal<>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(DateUtils.DEFAULT_JAVA_DATE_FORMAT);
        }
    };

    public static Optional<Date> parseDate(final String s, final SimpleDateFormat parser) {
        return DateUtils.parseDate(s, parser, DateUtils.DEFAULT_DATE_PARSER.get());
    }

    public static Optional<Date> parseDate(final String s, final SimpleDateFormat parser,
            final SimpleDateFormat defaultParser) {
        return DateUtils.tryParseDate(s, parser)
                .or(() -> DateUtils.tryParseDate(s, defaultParser));
    }

    public static Date dateToWeekDate(final Date date) {
        return DateUtils.dateToWeekDate(date, Locale.getDefault(), -1);
    }

    public static Date dateToWeekDate(final Date date, final Locale locale) {
        return DateUtils.dateToWeekDate(date, locale, -1);
    }

    public static Date dateToWeekDate(final Date date, final Locale locale, final int firstDayOfWeek) {
        final var calendar = Calendar.getInstance(locale);
        calendar.setTime(date);

        DateUtils.resetTime(calendar);

        if (firstDayOfWeek == -1) {
            calendar.set(Calendar.DAY_OF_WEEK, DateUtils.getFirstDayOfWeekFromLocale(locale));
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        }

        return calendar.getTime();
    }

    public static Date dateToMonthDate(final Date date) {
        return DateUtils.dateToMonthDate(date, 0, Locale.getDefault());
    }

    public static Date dateToMonthDate(final Date date, final int shiftDays) {
        return DateUtils.dateToMonthDate(date, shiftDays, Locale.getDefault());
    }

    public static Date dateToMonthDate(final Date date, final Locale locale) {
        return DateUtils.dateToMonthDate(date, 0, locale);
    }

    public static Date dateToMonthDate(final Date date, final int shiftDays, final Locale locale) {
        final var calendar = Calendar.getInstance(locale);
        calendar.setTime(date);

        if (shiftDays != 0) {
            DateUtils.addDaysSkippingWeekends(calendar, shiftDays);
        }

        DateUtils.resetTime(calendar);
        calendar.set(Calendar.DATE, 1);

        return calendar.getTime();
    }

    public static void resetTime(final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static Date quarterToMonth(final Date date, final Locale locale) {
        final var calendar = Calendar.getInstance(locale);
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) * 3);
        return calendar.getTime();
    }

    public static Date monthToQuarter(final Date date, final Locale locale) {
        final var calendar = Calendar.getInstance(locale);
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) / 3);
        return calendar.getTime();
    }

    private static void addDaysSkippingWeekends(final Calendar date, final int days) {
        for (var addedDays = 0; addedDays < days;) {
            date.add(Calendar.DATE, 1);
            if (DateUtils.isOpenedDay(date)) {
                ++addedDays;
            }
        }
    }

    private static boolean isOpenedDay(final Calendar date) {
        return !(date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    private static Optional<Date> tryParseDate(final String s, final SimpleDateFormat parser) {
        final var pos = new ParsePosition(0);
        final var result = Optional.ofNullable(parser.parse(s, pos));
        if (pos.getIndex() == 0) {
            return Optional.empty();
        } else {
            return result;
        }
    }

    private static int getFirstDayOfWeekFromLocale(final Locale locale) {
        switch (WeekFields.of(locale).getFirstDayOfWeek()) {
            case FRIDAY:
                return Calendar.FRIDAY;
            case MONDAY:
                return Calendar.MONDAY;
            case SATURDAY:
                return Calendar.SATURDAY;
            case SUNDAY:
                return Calendar.SUNDAY;
            case THURSDAY:
                return Calendar.THURSDAY;
            case TUESDAY:
                return Calendar.TUESDAY;
            case WEDNESDAY:
                return Calendar.WEDNESDAY;
            default:
                throw new RuntimeException("Invalid first day of the week");
        }
    }
}
