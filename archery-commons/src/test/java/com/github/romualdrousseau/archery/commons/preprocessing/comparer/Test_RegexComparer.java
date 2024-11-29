package com.github.romualdrousseau.archery.commons.preprocessing.comparer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test_RegexComparer {

    private Map<String, String> patterns;
    private List<String> bagOfWords;

    @BeforeAll
    public void setUp() {
        this.patterns = new HashMap<>();
        patterns.put("\\d{1,4}[/|.|-]\\d{1,2}[/|.|-]\\d{1,4}", "DATE");
        patterns.put("^-?[\\d,]+(\\.\\d+)?([e|E]-?\\d+)?$", "NUMBER");

        this.bagOfWords = Stream.of("1000.25", "Date: 2024-01-01", "some words", "2 words: word1 and word2", null).toList();
    }

    @Test
    @Tag("unit")
    public void testApplyNullValuesInList() {
        final var rc = new RegexComparer(this.patterns);
        assertFalse(rc.apply(null, this.bagOfWords));
    }

    @Test
    @Tag("unit")
    public void testApplyEmptyList() {
        final var rc = new RegexComparer(this.patterns);
        assertFalse(rc.apply("NUMBER", Collections.emptyList()));
    }

    @Test
    @Tag("unit")
    public void testApplyMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertTrue(rc.apply("NUMBER", this.bagOfWords));
        assertTrue(rc.apply("DATE", this.bagOfWords));
    }

    @Test
    @Tag("unit")
    public void testApplyNonMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertFalse(rc.apply("NUMBER", this.bagOfWords.stream().skip(2).toList()));
        assertFalse(rc.apply("DATE", this.bagOfWords.stream().skip(2).toList()));
    }

    @Test
    @Tag("unit")
    public void testAnonymizeMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertEquals("NUMBER", rc.anonymize(this.bagOfWords.get(0)));
        assertEquals("Date: DATE", rc.anonymize(this.bagOfWords.get(1)));
    }

    @Test
    @Tag("unit")
    public void testAnonymizeNonMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertEquals(this.bagOfWords.get(2), rc.anonymize(this.bagOfWords.get(2)));
        assertEquals(this.bagOfWords.get(3), rc.anonymize(this.bagOfWords.get(3)));
    }

    @Test
    @Tag("unit")
    public void testAnonymizeMatchingPatternWithFilter() {
        final var rc = new RegexComparer(this.patterns);
        assertEquals("NUMBER", rc.anonymize(this.bagOfWords.get(0), "NUMBER"));
        assertEquals("Date: DATE", rc.anonymize(this.bagOfWords.get(1), "DATE"));
    }

    @Test
    @Tag("unit")
    public void testAnonymizeNonMatchingPatternWithFilter() {
        final var rc = new RegexComparer(this.patterns);
        assertEquals(this.bagOfWords.get(0), rc.anonymize(this.bagOfWords.get(0), "DATE"));
        assertEquals(this.bagOfWords.get(1), rc.anonymize(this.bagOfWords.get(1), "NUMBER"));
    }

    @Test
    @Tag("unit")
    public void testFindMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertEquals("1000.25", rc.find(this.bagOfWords.get(0)).get());
        assertEquals("2024-01-01", rc.find(this.bagOfWords.get(1)).get());
    }

    @Test
    @Tag("unit")
    public void testFindNonMatchingPattern() {
        final var rc = new RegexComparer(this.patterns);
        assertTrue(rc.find(this.bagOfWords.get(2)).isEmpty());
        assertTrue(rc.find(this.bagOfWords.get(3)).isEmpty());
    }

    @Test
    @Tag("unit")
    public void testFindMatchingPatternWithFilter() {
        final var rc = new RegexComparer(this.patterns);
        final var expected = List.of("1000.25", "2024-01-01");
        assertEquals(expected.get(0), rc.find(this.bagOfWords.get(0), "NUMBER").get());
        assertEquals(expected.get(1), rc.find(this.bagOfWords.get(1), "DATE").get());
    }

    @Test
    @Tag("unit")
    public void testFindNonMatchingPatternWithFilter() {
        final var rc = new RegexComparer(this.patterns);
        assertTrue(rc.find(this.bagOfWords.get(0), "DATE").isEmpty());
        assertTrue(rc.find(this.bagOfWords.get(1), "NUMBER").isEmpty());
    }
}
