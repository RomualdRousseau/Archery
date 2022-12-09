package com.github.romualdrousseau.any2json.classifiers;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.IClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public class LayexAndEmbedClassifierBuilder implements IClassifierBuilder {

    public LayexAndEmbedClassifierBuilder setModel(JSONObject model) {
        this.model = model;
        return this;
    }

    public LayexAndEmbedClassifierBuilder setNgramList(NgramList ngramList) {
        this.ngramList = ngramList;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setEntityList(RegexList entityList) {
        this.entityList = entityList;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setStopWordList(StopWordList stopWordList) {
        this.stopWordList = stopWordList;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setTagList(StringList tagList) {
        this.tagList = tagList;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setRequiredTagList(String[] requiredTagList) {
        this.requiredTagList = requiredTagList;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setPivotEntityList(String[] pivotEntityList) {
        this.pivotEntityList = pivotEntityList;
		return this;
	}

    public LayexAndEmbedClassifierBuilder setMetaLayexes(String[] metaLayexes) {
        this.metaLayexes = metaLayexes;
		return this;
    }

    public LayexAndEmbedClassifierBuilder setDataLayexes(String[] dataLayexes) {
        this.dataLayexes = dataLayexes;
		return this;
	}

    public ClassifierFactory build() {
        final LayexAndEmbedClassifier classifier;
        if (model == null) {
            if (metaLayexes != null && dataLayexes != null) {
                classifier = new LayexAndEmbedClassifier(ngramList, entityList, stopWordList, tagList, requiredTagList, pivotEntityList, metaLayexes, dataLayexes);
            } else {
                classifier = new LayexAndEmbedClassifier(ngramList, entityList, stopWordList, tagList, requiredTagList, pivotEntityList);
            }
        } else {
            classifier = new LayexAndEmbedClassifier(model);
        }
        return new ClassifierFactory()
            .setLayoutClassifier(classifier)
            .setTagClassifier(classifier);
    }

    private JSONObject model;
    private NgramList ngramList;
    private RegexList entityList;
    private StopWordList stopWordList;
    private StringList tagList;
    private String[] requiredTagList;
    private String[] pivotEntityList;
    private String[] metaLayexes;
    private String[] dataLayexes;
}
