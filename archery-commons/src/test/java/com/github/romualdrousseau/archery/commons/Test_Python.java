package com.github.romualdrousseau.archery.commons;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.github.romualdrousseau.archery.commons.python.PythonSimpleDateFormat;

public class Test_Python {

    @Test
    @Tag("unit")
    public void testPythonSimpleDateformat() throws ParseException {
        final PythonSimpleDateFormat formatter = new PythonSimpleDateFormat("%a,%d/%m/%y");
        assertEquals("Sun,24/09/23", formatter.format(Date.from(LocalDate.of(2023, 9, 24).atStartOfDay(ZoneId.systemDefault()).toInstant())));
        assertEquals("Sun,05/12/99", formatter.format(formatter.parse("Sun,05/12/99")));
    }
}
