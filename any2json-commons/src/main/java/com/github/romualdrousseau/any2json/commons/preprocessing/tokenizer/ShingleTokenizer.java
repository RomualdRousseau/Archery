package com.github.romualdrousseau.any2json.commons.preprocessing.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.commons.preprocessing.Text;
import com.github.romualdrousseau.any2json.commons.strings.StringUtils;

public class ShingleTokenizer implements Text.ITokenizer {

    private static final int MIN_SIZE = 2;

    private static final ThreadLocal<Pattern> CAMEL_PATTERN = new ThreadLocal<Pattern>() {
        @Override
        protected Pattern initialValue() {
            return Pattern.compile("(?<!(^|[A-Z/]))(?=[A-Z/])|(?<!^)(?=[A-Z/][a-z/])");
        }
    };

    private final List<Map.Entry<String, List<String>>> variants;
    private final int minSize;

    private boolean lemmatization;

    public ShingleTokenizer(final List<String> lexicon) {
        this(lexicon, MIN_SIZE);
    }

    public ShingleTokenizer(final List<String> lexicon, final int minSize) {
        this(lexicon, minSize, true);
    }

    public ShingleTokenizer(final List<String> lexicon, final int minSize, final boolean lemmatization) {
        this.variants = Text.get_lexicon(lexicon).entrySet().stream()
                .sorted((a, b) -> b.getKey().length() - a.getKey().length()).toList();
        this.minSize = minSize;
        this.lemmatization = lemmatization;
    }

    public void enableLemmatization() {
        this.lemmatization = true;
    }

    public void disableLemmatization() {
        this.lemmatization = false;
    }

    @Override
    public List<String> apply(final String w) {
        var s = StringUtils.normalizeWhiteSpaces(w);

        // Split using a lexicon of known words if any and prioritize longest variant

        final var lexems = this.variants.stream().collect(Collectors.toList());
        while (lexems.size() > 0) {
            final var lexem = lexems.remove(0);
            for (final String variant : lexem.getValue()) {
                if (s.toLowerCase().contains(variant)) {
                    final var replacement = this.lemmatization ? lexem.getKey() : variant;
                    s = s.replaceAll("(?i)" + variant, " " + replacement + " ");
                    lexems.removeIf(x -> x.getValue().stream().anyMatch(y -> replacement.contains(y)));
                    break;
                }
            }
        }

        // Clean by space and underscore

        s = s.replaceAll("[\\s_]+", " ").trim();

        // Split by space and then by Camel notation words

        final ArrayList<String> result = new ArrayList<String>();
        for (final String ss : s.split(" ")) {
            for (final String sss : CAMEL_PATTERN.get().split(ss)) {
                if (sss.length() > 0 && (sss.length() > (minSize - 1) || !Character.isAlphabetic(sss.charAt(0)))) {
                    result.add(sss.toLowerCase());
                }
            }
        }

        return result;
    }
}
