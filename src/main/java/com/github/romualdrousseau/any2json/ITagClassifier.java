package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public interface ITagClassifier {

    NgramList getWordList();

    StringList getTagList();

    Iterable<String> getRequiredTagList();

    float getMean();

    float getAccuracy();

    DataRow buildPredictRow(final String name, final Iterable<String> entities, final Iterable<String> context);

    DataRow buildTrainingRow(final String name, final Iterable<String> entities, final Iterable<String> context, final String tag, final boolean ensureWordsExists);

    void fit(DataSet trainingSet, DataSet validationSet);

    String predict(DataRow input);
}
