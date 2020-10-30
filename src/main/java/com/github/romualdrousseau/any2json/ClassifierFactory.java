package com.github.romualdrousseau.any2json;

import java.util.Optional;

public class ClassifierFactory {

    public static ClassifierFactory get() {
        if (ClassifierFactory.singleton == null) {
            ClassifierFactory.singleton = new ClassifierFactory();
        }
        return ClassifierFactory.singleton;
    }

    public ClassifierFactory setLayoutClassifier(ILayoutClassifier layoutClassifier) {
        this.layoutClassifier = layoutClassifier;
        return this;
    }

    public ClassifierFactory setTagClassifier(ITagClassifier tagClassifier) {
        this.tagClassifier = tagClassifier;
        return this;
    }

    public Optional<ILayoutClassifier> getLayoutClassifier() {
        return (this.layoutClassifier == null) ? Optional.empty() : Optional.of(this.layoutClassifier);
    }

    public Optional<ITagClassifier> getTagClassifier() {
        return (this.tagClassifier == null) ? Optional.empty() : Optional.of(this.tagClassifier);
    }

    private static ClassifierFactory singleton;
    private ILayoutClassifier layoutClassifier;
    private ITagClassifier tagClassifier;
}
