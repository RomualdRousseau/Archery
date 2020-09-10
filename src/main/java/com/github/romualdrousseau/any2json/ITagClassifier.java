package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.base.TableMatcher;
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

    List<String> getRequiredTagList();

    List<TableMatcher> getMetaLayexes();

    List<TableMatcher> getDataLayexes();

    List<String> getPivotEntityList();

    float getMean();

    float getAccuracy();

    void fit(DataSet trainingSet, DataSet validationSet);

    String predict(DataRow input);
}
