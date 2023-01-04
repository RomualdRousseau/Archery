package com.github.romualdrousseau.any2json.classifier;

import com.github.romualdrousseau.any2json.ClassifierFactory;

public class SimpleClassifierBuilder {

    public ClassifierFactory build() {
        return new ClassifierFactory().setLayoutClassifier(null).setTagClassifier(null);
    }
}
