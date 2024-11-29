package com.github.romualdrousseau.archery.commons.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test_DateUtils {

    private SimpleDateFormat dateParser;
    private SimpleDateFormat weekMondayParser;
    private SimpleDateFormat weekSundayParser;

    @BeforeAll
    public void setUp() {
        this.dateParser = new SimpleDateFormat("yyyy/MM/dd");
        this.weekMondayParser = new SimpleDateFormat("yyyy/ww", Locale.UK);
        this.weekSundayParser = new SimpleDateFormat("yyyy/ww", Locale.US);
    }

    @Test
    @Tag("unit")
    public void testParseDate() throws Exception {
        final var expectedDate = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-01");
        assertEquals(expectedDate, DateUtils.parseDate("2024/11/01", this.dateParser).get());
    }

    @Test
    @Tag("unit")
    public void testParseDateFailToDefaultFormat() throws Exception {
        final var expectedDate = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-01");
        assertEquals(expectedDate, DateUtils.parseDate("2024-11-01", this.dateParser).get());
    }

    @Test
    @Tag("unit")
    public void testParseDateWithWrongFormat() throws Exception {
        assertFalse(DateUtils.parseDate("2024 11 01", this.dateParser).isPresent());
    }

    @Test
    @Tag("unit")
    public void testestWeekSunday() throws Exception {
        final var expectedDate1 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2021-12-19");
        assertEquals(expectedDate1, DateUtils.parseDate("2021/52", this.weekSundayParser).get());

        final var expectedDate2 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2021-12-26");
        assertEquals(expectedDate2, DateUtils.parseDate("2022/01", this.weekSundayParser).get());
    }

    @Test
    @Tag("unit")
    public void testestWeekMonday() throws Exception {
        final var expectedDate1 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2021-12-27");
        assertEquals(expectedDate1, DateUtils.parseDate("2021/52", this.weekMondayParser).get());

        final var expectedDate2 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2022-01-03");
        assertEquals(expectedDate2, DateUtils.parseDate("2022/01", this.weekMondayParser).get());
    }

    @Test
    @Tag("unit")
    public void testDateToWeekDateSunday() throws Exception {
        final var someDate = DateUtils.parseDate("2024-11-26", this.dateParser).get();
        final var expectedDate = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-24");
        assertEquals(expectedDate, DateUtils.dateToWeekDate(someDate, Locale.US));
    }

    @Test
    @Tag("unit")
    public void testDateToWeekDateMonday() throws Exception {
        final var someDate = DateUtils.parseDate("2024-11-26", this.dateParser).get();
        final var expectedDate = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-25");
        assertEquals(expectedDate, DateUtils.dateToWeekDate(someDate, Locale.UK));
    }

    @Test
    @Tag("unit")
    public void testDateToMonthDate() throws Exception {
        final var someDate = DateUtils.parseDate("2024-11-24", this.dateParser).get();
        final var expectedDate = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-01");
        assertEquals(expectedDate, DateUtils.dateToMonthDate(someDate, 0, Locale.getDefault()));
    }

    @Test
    @Tag("unit")
    public void testDateToMonthDateWith3ShiftDays() throws Exception {
        final var someDate1 = DateUtils.parseDate("2024-11-26", this.dateParser).get();
        final var expectedDate1 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-11-01");
        assertEquals(expectedDate1, DateUtils.dateToMonthDate(someDate1, 3, Locale.getDefault()));

        final var someDate2 = DateUtils.parseDate("2024-11-27", this.dateParser).get();
        final var expectedDate2 = DateUtils.DEFAULT_DATE_PARSER.get().parse("2024-12-01");
        assertEquals(expectedDate2, DateUtils.dateToMonthDate(someDate2, 3, Locale.getDefault()));
    }
}
