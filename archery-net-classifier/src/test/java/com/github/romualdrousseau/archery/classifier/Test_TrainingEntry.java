package com.github.romualdrousseau.archery.classifier;

import org.junit.Test;

import java.util.List;

/**
 * Unit test for the TrainingEntry class.
 */
public class Test_TrainingEntry {

    @Test
    public void testTraininEntryEqualities() {
        final var e1 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e2 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e3 = new TrainingEntry(List.of(1, 2, 3, 4, 5, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e4 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(0, 1, 0, 0, 0));
        assert e1.equals(e1);
        assert e1.equals(e2);
        assert !e1.equals(e3);
        assert !e1.equals(e4);
    }

    @Test
    public void testTraininEntryConflicts() {
        final var e1 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e2 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(0, 1, 0, 0, 0));
        assert e1.isConflict(e2);
    }

    @Test
    public void testTraininEntryIndexOfOk() {
        final var e1 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e2 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e3 = new TrainingEntry(List.of(1, 2, 3, 4, 5, 0, 0), List.of(1, 0, 0, 0, 0));
        final var l = List.of(e1, e2, e3);
        assert l.indexOf(e3) == 2;
    }

    @Test
    public void testTraininEntryIndexOfNg() {
        final var e1 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e2 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e3 = new TrainingEntry(List.of(1, 2, 3, 4, 5, 0, 0), List.of(1, 0, 0, 0, 0));
        final var e4 = new TrainingEntry(List.of(1, 2, 3, 4, 0, 0, 0), List.of(0, 1, 0, 0, 0));
        final var l = List.of(e1, e2, e3);
        assert l.indexOf(e4) == -1;
    }
}
