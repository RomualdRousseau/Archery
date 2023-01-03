package com.github.romualdrousseau.any2json;

import java.util.Optional;

public class ClassifierFactory<T> {

    public ClassifierFactory<T> setLayoutClassifier(ILayoutClassifier layoutClassifier) {
        this.layoutClassifier = layoutClassifier;
        return this;
    }

    public ClassifierFactory<T> setTagClassifier(ITagClassifier<T> tagClassifier) {
        this.tagClassifier = tagClassifier;
        return this;
    }

    public Optional<ILayoutClassifier> getLayoutClassifier() {
        return Optional.ofNullable(this.layoutClassifier);
    }

    public Optional<ITagClassifier<T>> getTagClassifier() {
        return Optional.ofNullable(this.tagClassifier);
    }

    private ILayoutClassifier layoutClassifier;
    private ITagClassifier<T> tagClassifier;
}
