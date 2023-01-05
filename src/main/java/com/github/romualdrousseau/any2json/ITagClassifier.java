package com.github.romualdrousseau.any2json;

import java.util.List;

import com.github.romualdrousseau.shuju.json.JSONObject;

public interface ITagClassifier<T> {

    List<String> getTagList();

    List<String> getRequiredTagList();

    T buildPredictSet(final String name, final List<String> entities, final List<String> context);

    String predict(final T predictSet);

    boolean fit(List<T> trainingSet, List<T> validationSet);

    float getAccuracy();

    float getMean();

    JSONObject toJSON();
}
