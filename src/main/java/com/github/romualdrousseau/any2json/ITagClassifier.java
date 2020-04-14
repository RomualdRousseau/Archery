package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.layex.LayexMatcher;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.DataSet;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

import java.util.List;

public interface ITagClassifier {
    int getSampleCount();

    StopWordList getStopWordList();

    RegexList getEntityList();

    NgramList getWordList();

    StringList getTagList();

    String[] getRequiredTagList();

    List<LayexMatcher> getMetaLayexes();

    List<LayexMatcher> getDataLayexes();

    float getMean();

    float getAccuracy();

    void fit(DataSet trainingSet);

    String predict(DataRow input);
}
