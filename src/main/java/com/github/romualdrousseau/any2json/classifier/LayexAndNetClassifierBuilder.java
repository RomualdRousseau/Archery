package com.github.romualdrousseau.any2json.classifier;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.IClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSONObject;

public class LayexAndNetClassifierBuilder implements IClassifierBuilder {

    public LayexAndNetClassifierBuilder() {
    }

    public LayexAndNetClassifierBuilder setModel(final JSONObject model) {
        this.model = model;
        return this;
    }

    public LayexAndNetClassifierBuilder setVocabulary(final List<String> vocabulary) {
        this.vocabulary = vocabulary;
        return this;
    }

    public LayexAndNetClassifierBuilder setNGrams(final int ngrams) {
        this.ngrams = ngrams;
        return this;
    }

    public LayexAndNetClassifierBuilder setWordMinSize(final int wordMinSize) {
        this.wordMinSize = wordMinSize;
        return this;
    }

    public LayexAndNetClassifierBuilder setLexicon(final List<String> lexicon) {
        this.lexicon = lexicon;
        return this;
    }

    public LayexAndNetClassifierBuilder setEntityList(final List<String> entityList) {
        this.entityList = entityList;
        return this;
    }

    public LayexAndNetClassifierBuilder setPatternList(final Map<String, String> patternList) {
        this.patternList = patternList;
        return this;
    }

    public LayexAndNetClassifierBuilder setFilters(final List<String> filters) {
        this.filters = filters;
        return this;
    }

    public LayexAndNetClassifierBuilder setTagList(final List<String> tagList) {
        this.tagList = tagList;
        return this;
    }

    public LayexAndNetClassifierBuilder setRequiredTagList(final List<String> requiredTagList) {
        this.requiredTagList = requiredTagList;
        return this;
    }

    public LayexAndNetClassifierBuilder setPivotEntityList(final List<String> pivotEntityList) {
        this.pivotEntityList = pivotEntityList;
        return this;
    }

    public LayexAndNetClassifierBuilder setMetaLayexes(final List<String> metaLayexes) {
        this.metaLayexes = metaLayexes;
        return this;
    }

    public LayexAndNetClassifierBuilder setDataLayexes(final List<String> dataLayexes) {
        this.dataLayexes = dataLayexes;
        return this;
    }

    public LayexAndNetClassifierBuilder setModelPath(Path modelPath) {
        this.modelPath = modelPath;
        return this;
    }

    public ClassifierFactory build() {
        final LayexAndNetClassifier classifier;
        if (model == null) {
            classifier = new LayexAndNetClassifier(vocabulary, ngrams, wordMinSize, lexicon, entityList, patternList, filters, tagList,
                    requiredTagList, pivotEntityList, metaLayexes, dataLayexes, modelPath);
        } else {
            classifier = new LayexAndNetClassifier(model);
        }
        final ClassifierFactory classifierFactory = new ClassifierFactory();
        classifierFactory.setLayoutClassifier(classifier).setTagClassifier(classifier);
        return classifierFactory;
    }

    private JSONObject model;
    private List<String> vocabulary;
    private int ngrams;
    private int wordMinSize;
    private List<String> lexicon;
    private List<String> entityList;
    private Map<String, String> patternList;
    private List<String> filters;
    private List<String> tagList;
    private List<String> requiredTagList;
    private List<String> pivotEntityList;
    private List<String> metaLayexes;
    private List<String> dataLayexes;
    private Path modelPath;
}
