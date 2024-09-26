package com.github.romualdrousseau.any2json.commons.python;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PythonSimpleDateFormat extends SimpleDateFormat {

    public PythonSimpleDateFormat() {
        super();
    }

    public PythonSimpleDateFormat(final String pattern) {
        super(PythonSimpleDateFormat.toJava(pattern));
    }

    public PythonSimpleDateFormat(final String pattern, DateFormatSymbols formatSymbols) {
        super(PythonSimpleDateFormat.toJava(pattern), formatSymbols);
    }

    public PythonSimpleDateFormat(final String pattern, Locale locale) {
        super(PythonSimpleDateFormat.toJava(pattern), locale);
    }

    public static String toPython(final String javaPattern) {
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

    public static String toJava(final String pythonPattern) {
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
            .replaceAll("%V", "ww")
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
