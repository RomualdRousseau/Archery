package com.github.romualdrousseau.archery.commons.python;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

public class Test_Python {

    @Test
    @Tag("unit")
    public void testPythonSimpleDateformat() throws ParseException {
        final var formatter = new PythonSimpleDateFormat("%a,%d/%m/%y");
        assertEquals("Sun,24/09/23", formatter.format(Date.from(LocalDate.of(2023, 9, 24).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        assertEquals("Sun,05/12/99", formatter.format(formatter.parse("Sun,05/12/99")));
    }

    @Test
    @Tag("unit")
    public void testPythonSimpleDateformatWithLocale() throws ParseException {
        final var formatterGB = new PythonSimpleDateFormat("%b, %Y", Locale.forLanguageTag("en-GB"));
        assertEquals("Sept, 2023", formatterGB.format(formatterGB.parse("Sept, 2023")));

        final var formatterUS = new PythonSimpleDateFormat("%b, %Y", Locale.forLanguageTag("en-US"));
        assertEquals("Sep, 2023", formatterUS.format(formatterUS.parse("Sep, 2023")));

        final var formatterUS2 = new PythonSimpleDateFormat("'Year' %Y-%b %Y", Locale.forLanguageTag("en-US"));
        assertEquals("Oct, 2024", formatterUS.format(formatterUS2.parse("Year 2024-Oct 2024")));

        final var formatterHU = new PythonSimpleDateFormat("%Y. %B", Locale.forLanguageTag("hu-HU"));
        assertEquals("2024. október", formatterHU.format(formatterHU.parse("2024. Október")));
    }
}
