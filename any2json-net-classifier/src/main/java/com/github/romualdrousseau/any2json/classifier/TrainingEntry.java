package com.github.romualdrousseau.any2json.classifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class TrainingEntry {

    public static TrainingEntry of(final String name, final List<String> entities, final List<String> context,
            final String label, final Trainable trainable) {
        return new TrainingEntry(
                trainable.getInputVector(name, entities, context),
                trainable.getOutputVector(label));
    }

    private List<Integer> value;
    private List<Integer> label;

    public TrainingEntry(final List<Integer> value, final List<Integer> label) {
        this.value = value;
        this.label = label;
    }

    public List<Integer> getValue() {
        return this.value;
    }

    public void setValue(final List<Integer> value) {
        this.value = value;
    }

    public List<Integer> getLabel() {
        return this.label;
    }

    public void setLabel(final List<Integer> label) {
        this.label = label;
    }

    public List<Integer> getEntity() {
        return this.value.subList(0, NetTagClassifier.IN_ENTITY_SIZE);
    }

    public List<Integer> getName() {
        return this.value.subList(NetTagClassifier.IN_ENTITY_SIZE,
                NetTagClassifier.IN_ENTITY_SIZE + NetTagClassifier.IN_NAME_SIZE);
    }

    public List<Integer> getContext() {
        return this.value.subList(NetTagClassifier.IN_ENTITY_SIZE + NetTagClassifier.IN_NAME_SIZE,
                NetTagClassifier.IN_ENTITY_SIZE + NetTagClassifier.IN_NAME_SIZE + NetTagClassifier.IN_CONTEXT_SIZE);
    }

    public List<Integer> getVector() {
        return Stream.of(this.value, this.label).flatMap(Collection::stream).toList();
    }

    public boolean isConflict(final TrainingEntry other) {
        return this.value.equals(other.value) && !this.label.equals(other.label);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TrainingEntry)) {
            return false;
        }
        final var otherEntry = (TrainingEntry) other;
        return this.value.equals(otherEntry.value) && this.label.equals(otherEntry.label);
    }

    @Override
    public int hashCode() {
        return value.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return "[" + value.toString() + ", " + this.label.toString() + "]";
    }
}
