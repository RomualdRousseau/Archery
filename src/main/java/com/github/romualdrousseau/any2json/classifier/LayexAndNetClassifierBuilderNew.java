package com.github.romualdrousseau.any2json.classifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.IClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSONObject;

public class LayexAndNetClassifierBuilderNew implements IClassifierBuilder {

    public LayexAndNetClassifierBuilderNew() {
        this.requiredTagList = Collections.emptyList();
        this.pivotEntityList = Collections.emptyList();
        this.metaLayexes = Collections.emptyList();
        this.dataLayexes = Collections.emptyList();
    }

    public LayexAndNetClassifierBuilderNew setModel(final JSONObject model) {
        this.model = model;
        return this;
    }

    public LayexAndNetClassifierBuilderNew setVocabulary(final List<String> vocabulary) {
        this.vocabulary = vocabulary;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setLexicon(final List<String> lexicon) {
        this.lexicon = lexicon;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setEntityList(final List<String> entityList) {
        this.entityList = entityList;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setPatternList(final Map<String, String> patternList) {
        this.patternList = patternList;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setFilters(final List<String> filters) {
        this.filters = filters;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setTagList(final List<String> tagList) {
        this.tagList = tagList;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setRequiredTagList(final List<String> requiredTagList) {
        this.requiredTagList = requiredTagList;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setPivotEntityList(final List<String> pivotEntityList) {
        this.pivotEntityList = pivotEntityList;
		return this;
	}

    public LayexAndNetClassifierBuilderNew setMetaLayexes(final List<String> metaLayexes) {
        this.metaLayexes = metaLayexes;
		return this;
    }

    public LayexAndNetClassifierBuilderNew setDataLayexes(final List<String> dataLayexes) {
        this.dataLayexes = dataLayexes;
		return this;
	}

    public ClassifierFactory<List<Integer>> build() {
        final LayexAndNetClassifierNew classifier;
        if (model == null) {
            classifier = new LayexAndNetClassifierNew(vocabulary, lexicon, entityList, patternList, filters, tagList, requiredTagList, pivotEntityList, metaLayexes, dataLayexes);
        } else {
            classifier = new LayexAndNetClassifierNew(model);
        }
        return new ClassifierFactory<List<Integer>>().setLayoutClassifier(classifier).setTagClassifier(classifier);
    }

    private JSONObject model;
    private List<String> vocabulary;
    private List<String> lexicon;
    private List<String> entityList;
    private Map<String, String> patternList;
    private List<String> filters;
    private List<String> tagList;
    private List<String> requiredTagList;
    private List<String> pivotEntityList;
    private List<String> metaLayexes;
    private List<String> dataLayexes;
}
