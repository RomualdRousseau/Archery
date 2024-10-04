package com.github.romualdrousseau.archery.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Tag;

import com.github.romualdrousseau.archery.commons.preprocessing.Text;
import com.github.romualdrousseau.archery.commons.preprocessing.comparer.RegexComparer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test_Text {

    private Map<String, String> patterns;
    private List<String> classes;
    private List<String> bagOfWords;

    @BeforeAll
    public void setUp() {
        this.patterns = new HashMap<>();
        patterns.put("\\d{1,4}[/|.|-]\\d{1,2}[/|.|-]\\d{1,4}", "DATE");
        patterns.put("^-?[\\d,]+(\\.\\d+)?([e|E]-?\\d+)?$", "NUMBER");

        this.classes = List.of("DATE", "NUMBER");

        this.bagOfWords = Stream.of("1000.25", "Date: 2024-01-01", "some words", "2 words: word1 and word2", null)
                .toList();
    }

    @Test
    @Tag("unit")
    public void testAllWords() {
        final var expected = List.of("01", "1000", "2", "2024", "25", "and", "date", "some", "word1", "word2", "words");
        assertEquals(expected, Text.all_words(this.bagOfWords));
    }

    @Test
    @Tag("unit")
    public void testToWords() {
        final var expected = List.of("2", "words", "word1", "and", "word2");
        assertEquals(expected, Text.to_words(this.bagOfWords.get(3)));
    }

    @Test
    @Tag("unit")
    public void testToCategorical() {
        final var comparer = new RegexComparer(this.patterns);
        assertEquals(List.of(0, 1), Text.to_categorical(this.bagOfWords.get(0), this.classes, comparer));
        assertEquals(List.of(1, 0), Text.to_categorical(this.bagOfWords.get(1), this.classes, comparer));
        assertEquals(List.of(0, 0), Text.to_categorical(this.bagOfWords.get(2), this.classes, comparer));
        assertEquals(List.of(1, 1), Text.to_categorical(this.bagOfWords, this.classes, comparer));
    }

    @Test
    @Tag("unit")
    public void testAnonymize() {
        final var comparer = new RegexComparer(this.patterns);
        final var expected = Stream.of("NUMBER", "Date: DATE", "some words", "2 words: word1 and word2", null).toList();
        assertEquals(expected.get(0), Text.anonymize(this.bagOfWords.get(0), comparer));
        assertEquals(expected.get(1), Text.anonymize(this.bagOfWords.get(1), comparer));
        assertEquals(expected.get(2), Text.anonymize(this.bagOfWords.get(2), comparer));
        assertEquals(expected, Text.anonymize(this.bagOfWords, comparer));
    }

    @Test
    @Tag("unit")
    public void testOnehot() {
        final var expected = List.of("2", "words", "word1", "and", "word2").stream().map(x -> x.hashCode()).toList();
        assertEquals(expected, Text.one_hot(this.bagOfWords.get(3)));
    }

    @Test
    @Tag("unit")
    public void testPadSequence() {
        final var value = List.of(0, 1, 2, 3);
        final var expected = List.of(0, 1, 2, 3, 0, 0, 0, 0);
        assertEquals(expected, Text.pad_sequence(value, expected.size()));
    }

    @Test
    @Tag("unit")
    public void testMutateSequence() {
        final var value = List.of(0, 1, 2, 3);
        assertTrue(Text.mutate_sequence(value).size() < value.size());
        assertNotEquals(value, Text.mutate_sequence(value));
    }
}
