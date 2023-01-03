package com.github.romualdrousseau.any2json.classifier;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.shuju.DataRow;

public class SimpleClassifierBuilder {

    public ClassifierFactory<DataRow> build() {
        return new ClassifierFactory<DataRow>()
            .setLayoutClassifier(null)
            .setTagClassifier(null);
    }
}
