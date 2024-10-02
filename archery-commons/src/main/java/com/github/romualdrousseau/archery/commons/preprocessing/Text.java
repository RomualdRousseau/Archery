package com.github.romualdrousseau.archery.commons.preprocessing;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.romualdrousseau.archery.commons.types.CollectionUtils;
import com.github.romualdrousseau.archery.commons.json.JSON;
import com.github.romualdrousseau.archery.commons.json.JSONArray;
import com.github.romualdrousseau.archery.commons.preprocessing.hasher.DefaultHasher;
import com.github.romualdrousseau.archery.commons.preprocessing.tokenizer.DefaultTokenizer;
import com.github.romualdrousseau.archery.commons.preprocessing.comparer.DefaultComparer;

public class Text {

    public interface ITokenizer extends Function<String, List<String>> {
    }

    public interface IHasher extends Function<String, Integer> {
    }

    public interface IComparer extends BiFunction<String, List<String>, Boolean> {
        String anonymize(String v);
        String anonymize(final String v, final String pattern);
        Optional<String> find(String v);
        Optional<String> find(final String v, final String pattern);
    }

    public static ITokenizer DefaultTokenizer = new DefaultTokenizer();

    public static IHasher DefaultHasher = new DefaultHasher();

    public static IComparer DefaultComparer = new DefaultComparer();

    public static List<String> DefaultFilters = List.of("[\\\\!\"#$%&()*+,-./:;<=>?@\\[\\]^_`{|}~\\t\\n]");

    public static Comparator<String> ComparatorByLength = (a, b) -> b.length() - a.length();

    public static Map<String, List<String>> get_lexicon(List<String> lexicon) {
        return lexicon.stream()
                .map(w -> List.of(w.split(",")))
                .collect(Collectors.toMap(
                        w -> w.get(0),
                        w -> w.stream().distinct().sorted(Text.ComparatorByLength).toList()));
    }

    public static List<String> all_words(final List<String> documents) {
        return Text.all_words(documents, Text.DefaultFilters);
    }

    public static List<String> all_words(final List<String> documents, final List<String> filters) {
        return Text.all_words(documents, filters, Text.DefaultTokenizer);
    }

    public static List<String> all_words(final List<String> documents, final List<String> filters, final ITokenizer tokenizer) {
        return documents.stream()
                .flatMap(d -> d != null ? Text.to_words(d, filters, tokenizer).stream() : Stream.empty())
                .distinct().sorted().toList();
    }

    public static List<String> to_words(final String text) {
        return Text.to_words(text, Text.DefaultFilters);
    }

    public static List<String> to_words(final String text, final List<String> filters) {
        return Text.to_words(text, filters, Text.DefaultTokenizer);
    }

    public static List<String> to_words(final String text, final List<String> filters, final ITokenizer tokenizer) {
        return tokenizer.apply(filters.stream().reduce(text, (a, x) -> a.replaceAll("(?i)" + x, " ")));
    }

    public static List<Integer> to_categorical(final String label, final List<String> classes) {
        return Text.to_categorical(label, classes, Text.DefaultComparer);
    }

    public static List<Integer> to_categorical(final String label, final List<String> classes,
            final IComparer comparer) {
        return Text.to_categorical(List.of(label), classes, comparer);
    }

    public static List<Integer> to_categorical(final List<String> labels, final List<String> classes) {
        return Text.to_categorical(labels, classes, Text.DefaultComparer);
    }

    public static List<Integer> to_categorical(final List<String> labels, final List<String> classes,
            final IComparer comparer) {
        return classes.stream().map(c -> comparer.apply(c, labels) ? 1 : 0).toList();
    }

    public static String anonymize(final String label, final IComparer comparer) {
        return Text.anonymize(List.of(label), comparer).get(0);
    }

    public static List<String> anonymize(final List<String> labels, final IComparer comparer) {
        return labels.stream().map(l -> comparer.anonymize(l)).toList();
    }

    public static List<Integer> one_hot(final String text) {
        return Text.one_hot(text, Text.DefaultFilters, Text.DefaultTokenizer, Text.DefaultHasher);
    }

    public static List<Integer> one_hot(final String text, final List<String> filters) {
        return Text.one_hot(text, filters, Text.DefaultTokenizer, Text.DefaultHasher);
    }

    public static List<Integer> one_hot(final String text, final List<String> filters, final ITokenizer tokenizer) {
        return Text.one_hot(text, filters, tokenizer, Text.DefaultHasher);
    }

    public static List<Integer> one_hot(final String text, final List<String> filters, final ITokenizer tokenizer, IHasher hasher) {
        return Text.to_words(text, filters, tokenizer).stream().map(hasher).toList();
    }

    public static List<Integer> pad_sequence(final List<Integer> sequence, final int maxLen) {
        return Text.pad_sequence(sequence, maxLen, 0);
    }

    public static List<Integer> pad_sequence(final List<Integer> sequence, final int maxLen, final int value) {
        final IntStream padding = IntStream.range(sequence.size(), maxLen).map(x -> value);
        return Stream.concat(sequence.stream(), padding.boxed()).toList();
    }

    public static List<Integer> mutate_sequence(final List<Integer> sequence) {
        return Text.mutate_sequence(sequence, 0.1f, 0);
    }

    public static List<Integer> mutate_sequence(final List<Integer> sequence, final float p) {
        return Text.mutate_sequence(sequence, p, 0);
    }

    public static List<Integer> mutate_sequence(final List<Integer> sequence, final float p, final int value) {
        final var shuffler = CollectionUtils.shuffle(CollectionUtils.mutableRange(0, sequence.size()));
        final Function<Integer, Integer> mutator = x -> Math.random() < p ? value : sequence.get(x);
        return shuffler.stream().map(mutator).filter(x -> x != value).toList();
    }

    public static JSONArray json_sequence(final List<Integer> sequence) {
        JSONArray result = JSON.newArray();
        sequence.forEach(x -> result.append(x));
        return result;
    }
}
