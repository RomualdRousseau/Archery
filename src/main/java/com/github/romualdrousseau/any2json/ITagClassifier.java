package com.github.romualdrousseau.any2json;

import java.util.List;

public interface ITagClassifier<T> {

    List<String> getTagList();

    List<String> getRequiredTagList();

    T buildPredictRow(final String name, final List<String> entities, final List<String> context);

    void fit(List<T> trainingSet, List<T> validationSet);

    String predict(final String name, final List<String> entities, final List<String> context);

    float getAccuracy();

    float getMean();

    
}
