package com.github.romualdrousseau.any2json.classifier;

import com.github.romualdrousseau.any2json.ClassifierFactory;

public class SimpleClassifierBuilder {

    public ClassifierFactory build() {
        final ClassifierFactory classifierFactory = new ClassifierFactory();
        classifierFactory.setLayoutClassifier(null).setTagClassifier(null);
        return classifierFactory;
    }
}
