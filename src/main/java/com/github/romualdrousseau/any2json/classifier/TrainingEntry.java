package com.github.romualdrousseau.any2json.classifier;

import java.util.List;

public class TrainingEntry {

    public TrainingEntry(final List<Integer> value, final List<Integer> label) {
        this.value = value;
        this.label = label;
    }

    private List<Integer> value;
    public List<Integer> getValue() {
        return this.value;
    }
    public void setValue(final List<Integer> value) {
        this.value = value;
    }

    private List<Integer> label;
    public List<Integer> getLabel() {
        return this.label;
    }
    public void setLabel(final List<Integer> label) {
        this.label = label;
    }

    public boolean isConflict(final TrainingEntry other) {
        return this.getValue().equals(other.getValue()) && !this.getLabel().equals(other.getLabel());
    }

    public boolean equals(final Object other) {
        if (!(other instanceof TrainingEntry)) {
            return false;
        }
        final var otherEntry = (TrainingEntry) other;
        return this.getValue().equals(otherEntry.getValue()) && this.getLabel().equals(otherEntry.getLabel());
    }

    public String toString() {
       return "[" + this.getValue().toString() + ", " + this.getLabel().toString() + "]";
    }
}
