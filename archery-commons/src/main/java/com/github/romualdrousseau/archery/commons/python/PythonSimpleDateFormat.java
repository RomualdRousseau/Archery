package com.github.romualdrousseau.archery.commons.python;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PythonSimpleDateFormat extends SimpleDateFormat {

    public static final String DEFAULT_PYTHON_DATE_FORMAT = "%Y-%m-%d";

    private final String pythonPattern;
    private final Locale locale;

    public PythonSimpleDateFormat(final String pythonPattern) {
        this(pythonPattern, Locale.getDefault());
    }

    public PythonSimpleDateFormat(final String pythonPattern, final Locale locale) {
        super(PythonSimpleDateFormat.toJavaPattern(pythonPattern), locale);
        this.pythonPattern = pythonPattern;
        this.locale = locale;
    }

    public String getPythonPattern() {
        return this.pythonPattern;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public int getFirstDayOfWeek() {
        if (this.pythonPattern.contains("%w") || this.pythonPattern.contains("%W")) {
            return Calendar.MONDAY;
        } else if (this.pythonPattern.contains("%u") || this.pythonPattern.contains("%U")) {
            return Calendar.SUNDAY;
        } else {
            return -1;
        }
    }

    @Override
    public Date parse(final String text, final ParsePosition pos) {
        if (this.pythonPattern.contains("%Q")) {
            return super.parse(text
                    .replace("Q1", "01-01")
                    .replace("Q2", "04-01")
                    .replace("Q3", "07-01")
                    .replace("Q4", "10-01"),
                    pos);
        } else {
            return super.parse(text, pos);
        }
    }

    @Override
    public StringBuffer format(final Date date, final StringBuffer buffer, final FieldPosition pos) {
        if (this.pythonPattern.contains("%Q")) {
            final var res = super.format(date, buffer, pos).toString();
            return new StringBuffer(res
                    .replace("01-01", "Q1")
                    .replace("04-01", "Q2")
                    .replace("07-01", "Q3")
                    .replace("10-01", "Q4"));
        } else {
            return super.format(date, buffer, pos);
        }
    }

    public static String toPythonPattern(final String javaPattern) {
        return javaPattern
                .replaceAll("YYYY", "%G")
                .replaceAll("yyyy", "%Y")
                .replaceAll("yy", "%y")
                .replaceAll("y", "%-y")
                .replaceAll("MMMMM", "%B")
                .replaceAll("MMM", "%b")
                .replaceAll("MM", "%m")
                .replaceAll("M", "%-m")
                .replaceAll("DDD", "%j")
                .replaceAll("dd", "%d")
                .replaceAll("d", "%-d")
                .replaceAll("EEEEE", "%A")
                .replaceAll("EEE", "%a")
                .replaceAll("ww", "%W")
                .replaceAll("w", "%W")
                .replaceAll("u", "%u")
                .replaceAll("HH", "%H")
                .replaceAll("H", "%-H")
                .replaceAll("hh", "%I")
                .replaceAll("h", "%-I")
                .replaceAll("mm", "%M")
                .replaceAll("m", "%-M")
                .replaceAll("ss", "%S")
                .replaceAll("s", "%-S");
    }

    public static String toJavaPattern(final String pythonPattern) {
        return pythonPattern
                .replaceAll("%G", "YYYY")
                .replaceAll("%Y", "yyyy")
                .replaceAll("%y", "yy")
                .replaceAll("%-y", "y")
                .replaceAll("%B", "MMMMM")
                .replaceAll("%b", "MMM")
                .replaceAll("%m", "MM")
                .replaceAll("%-m", "M")
                .replaceAll("%j", "DDD")
                .replaceAll("%d", "dd")
                .replaceAll("%-d", "d")
                .replaceAll("%A", "EEEEE")
                .replaceAll("%a", "EEE")
                .replaceAll("%W", "ww")
                .replaceAll("%U", "ww")
                .replaceAll("%V", "ww")
                .replaceAll("%w", "u")
                .replaceAll("%u", "u")
                .replaceAll("%H", "HH")
                .replaceAll("%-H", "H")
                .replaceAll("%I", "hh")
                .replaceAll("%-I", "h")
                .replaceAll("%M", "mm")
                .replaceAll("%-M", "m")
                .replaceAll("%S", "ss")
                .replaceAll("%-S", "s")
                .replaceAll("%Q", "MM-dd");
    }
}
