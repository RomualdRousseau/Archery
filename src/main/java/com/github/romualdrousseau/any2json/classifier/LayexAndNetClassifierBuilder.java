package com.github.romualdrousseau.any2json.classifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.IClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.nlp.NgramList;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.nlp.StopWordList;
import com.github.romualdrousseau.shuju.nlp.StringList;

public class LayexAndNetClassifierBuilder implements IClassifierBuilder {

    public LayexAndNetClassifierBuilder() {
        this.requiredTagList = Collections.emptyList();
        this.pivotEntityList = Collections.emptyList();
        this.metaLayexes = Collections.emptyList();
        this.dataLayexes = Collections.emptyList();
    }

    public LayexAndNetClassifierBuilder setModel(final JSONObject model) {
        this.model = model;
        return this;
    }

    public LayexAndNetClassifierBuilder setNgramList(final NgramList ngramList) {
        this.ngramList = ngramList;
		return this;
    }

    public LayexAndNetClassifierBuilder setEntityList(final RegexList entityList) {
        this.entityList = entityList;
		return this;
    }

    public LayexAndNetClassifierBuilder setStopWordList(final StopWordList stopWordList) {
        this.stopWordList = stopWordList;
		return this;
    }

    public LayexAndNetClassifierBuilder setTagList(final StringList tagList) {
        this.tagList = tagList;
		return this;
    }

    public LayexAndNetClassifierBuilder setRequiredTagList(final String[] requiredTagList) {
        this.requiredTagList = Arrays.asList(requiredTagList);
		return this;
    }

    public LayexAndNetClassifierBuilder setPivotEntityList(final String[] pivotEntityList) {
        this.pivotEntityList = Arrays.asList(pivotEntityList);
		return this;
	}

    public LayexAndNetClassifierBuilder setMetaLayexes(final String[] metaLayexes) {
        this.metaLayexes = Arrays.asList(metaLayexes);
		return this;
    }

    public LayexAndNetClassifierBuilder setDataLayexes(final String[] dataLayexes) {
        this.dataLayexes = Arrays.asList(dataLayexes);
		return this;
	}

    public LayexAndNetClassifierBuilder setRecipe(final String recipe) {
        this.recipe = recipe;
		return this;
	}

    public ClassifierFactory build() {
        final LayexAndNetClassifier classifier;
        if (model == null) {
            classifier = new LayexAndNetClassifier(ngramList, entityList, stopWordList, tagList, requiredTagList, pivotEntityList, metaLayexes, dataLayexes, recipe);
        } else {
            classifier = new LayexAndNetClassifier(model);
        }
        return new ClassifierFactory().setLayoutClassifier(classifier).setTagClassifier(classifier);
    }

    private JSONObject model;
    private NgramList ngramList;
    private RegexList entityList;
    private StopWordList stopWordList;
    private StringList tagList;
    private List<String> requiredTagList;
    private List<String> pivotEntityList;
    private List<String> metaLayexes;
    private List<String> dataLayexes;
    private String recipe;
}
