package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.any2json.layex.TableMatcher;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;

import java.util.List;

public interface ILayoutClassifier {

    int getSampleCount();

    RegexList getEntityList();

    StopWordList getStopWordList();

    List<String> getPivotEntityList();

    List<TableMatcher> getMetaMatcherList();

    List<TableMatcher> getDataMatcherList();
}
