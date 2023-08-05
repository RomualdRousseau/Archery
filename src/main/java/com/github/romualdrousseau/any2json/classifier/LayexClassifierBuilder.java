package com.github.romualdrousseau.any2json.classifier;

import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.IClassifierBuilder;
import com.github.romualdrousseau.shuju.json.JSONObject;

public class LayexClassifierBuilder implements IClassifierBuilder {

    public LayexClassifierBuilder() {
    }

    public LayexClassifierBuilder setModel(final JSONObject model) {
        this.model = model;
        return this;
    }

    public LayexClassifierBuilder setEntityList(final List<String> entityList) {
        this.entityList = entityList;
        return this;
    }

    public LayexClassifierBuilder setPatternList(final Map<String, String> patternList) {
        this.patternList = patternList;
        return this;
    }

    public LayexClassifierBuilder setFilters(final List<String> filters) {
        this.filters = filters;
        return this;
    }

    public LayexClassifierBuilder setPivotEntityList(final List<String> pivotEntityList) {
        this.pivotEntityList = pivotEntityList;
        return this;
    }

    public LayexClassifierBuilder setMetaLayexes(final List<String> metaLayexes) {
        this.metaLayexes = metaLayexes;
        return this;
    }

    public LayexClassifierBuilder setDataLayexes(final List<String> dataLayexes) {
        this.dataLayexes = dataLayexes;
        return this;
    }

    public ClassifierFactory build() {
        final LayexClassifier classifier;
        if (model == null) {
            classifier = new LayexClassifier(entityList, patternList, filters, pivotEntityList, metaLayexes, dataLayexes);
        } else {
            classifier = new LayexClassifier(model);
        }
        final ClassifierFactory classifierFactory = new ClassifierFactory();
        classifierFactory.setLayoutClassifier(classifier).setTagClassifier(null);
        return classifierFactory;
    }

    private JSONObject model;
    private List<String> entityList;
    private Map<String, String> patternList;
    private List<String> filters;
    private List<String> pivotEntityList;
    private List<String> metaLayexes;
    private List<String> dataLayexes;
}
