package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.nlp.BaseList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;

public interface ITagClassifier {
    int getSampleCount();

    StopWordList getStopWordList();

    BaseList getEntityList();

    BaseList getWordList();

    BaseList getTagList();

    float getMean();

    float getAccuracy();

    void fit(DataSet trainingSet);

    String predict(DataRow input);
}
