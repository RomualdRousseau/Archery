package com.github.romualdrousseau.any2json.commons.preprocessing.comparer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.romualdrousseau.any2json.commons.preprocessing.Text;

public class RegexComparer implements Text.IComparer {

    private final Map<String, String> patterns;
    private final Map<String, Pattern> compiledPatterns;

    public RegexComparer(final Map<String, String> patterns) {
        this.patterns = patterns;
        this.compiledPatterns = patterns.keySet().stream()
                .collect(Collectors.toUnmodifiableMap(r -> r, this::compileRegex));
    }

    @Override
    public Boolean apply(final String a, final List<String> b) {
        return (a == null) ? false
                : this.patterns.entrySet().stream()
                        .filter(p -> p.getValue().equals(a))
                        .map(p -> this.compiledPatterns.get(p.getKey()).matcher(""))
                        .anyMatch(m -> b.stream().anyMatch(v -> v != null && m.reset(v).find()));
    }

    @Override
    public String anonymize(final String v) {
        return (v == null) ? null
                : this.patterns.entrySet().stream()
                        .reduce(v, (r, e) -> this.compiledPatterns.get(e.getKey()).matcher(r).replaceAll(e.getValue()),
                                (res1, res2) -> res1);
    }

    @Override
    public String anonymize(final String v, final String filter) {
        return (v == null) ? null
                : this.patterns.entrySet().stream()
                        .filter(e -> e.getValue().equals(filter))
                        .reduce(v, (r, e) -> this.compiledPatterns.get(e.getKey()).matcher(r).replaceAll(e.getValue()),
                                (res1, res2) -> res1);
    }

    @Override
    public Optional<String> find(final String v) {
        return (v == null) ? Optional.empty()
                : this.compiledPatterns.values().stream()
                        .map(e -> e.matcher(v))
                        .filter(m -> m.find())
                        .map(m -> m.group())
                        .findFirst();
    }

    @Override
    public Optional<String> find(final String v, final String filter) {
        return (v == null) ? Optional.empty()
                : this.patterns.entrySet().stream()
                        .filter(p -> p.getValue().equals(filter))
                        .map(p -> this.compiledPatterns.get(p.getKey()).matcher(v))
                        .filter(m -> m.find())
                        .map(m -> m.group())
                        .findFirst();
    }

    private Pattern compileRegex(final String r) {
        return Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
