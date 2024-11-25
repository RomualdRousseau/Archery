package com.github.romualdrousseau.archery.commons.python;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PythonSimpleDateFormat extends SimpleDateFormat {

    private final String pythonPattern;
    private final Locale locale;

    public PythonSimpleDateFormat(final String pythonPattern) {
        this(pythonPattern, Locale.getDefault());
    }

    public PythonSimpleDateFormat(final String pythonPattern, Locale locale) {
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
                .replaceAll("%w", "u")
                .replaceAll("%u", "u")
                .replaceAll("%U", "ww")
                .replaceAll("%H", "HH")
                .replaceAll("%-H", "H")
                .replaceAll("%I", "hh")
                .replaceAll("%-I", "h")
                .replaceAll("%M", "mm")
                .replaceAll("%-M", "m")
                .replaceAll("%S", "ss")
                .replaceAll("%-S", "s");
    }
}
