package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.StringList;

import java.util.List;

public interface ITagClassifier {

    NgramList getWordList();

    StringList getTagList();

    List<String> getRequiredTagList();

    float getMean();

    float getAccuracy();

    void fit(DataSet trainingSet, DataSet validationSet);

    String predict(DataRow input);
}
