package com.github.romualdrousseau.any2json;

import java.util.List;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;

public interface ITagClassifier {

    List<String> getWordList();

    List<String> getTagList();

    List<String> getRequiredTagList();

    float getMean();

    float getAccuracy();

    DataRow buildTrainingRow(final String name, final Iterable<String> entities, final Iterable<String> context, final String tag, final boolean ensureWordsExists);

    DataRow buildPredictRow(final String name, final Iterable<String> entities, final Iterable<String> context);

    void fit(DataSet trainingSet, DataSet validationSet);

    String predict(DataRow input);
}
