package com.github.romualdrousseau.any2json;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

import com.github.romualdrousseau.shuju.json.JSONObject;

public interface ITagClassifier<T> extends AutoCloseable
{
    List<String> getTagList();

    List<String> getRequiredTagList();

    T buildPredictSet(String name, List<String> entities, List<String> context);

    AbstractMap.SimpleImmutableEntry<T, T> buildTrainingSet(String name, List<String> entities, List<String> context, String label);

    String predict(T predictSet);

    Process fit(List<T> trainingSet, List<T> validationSet) throws IOException;

    JSONObject toJSON();
}
