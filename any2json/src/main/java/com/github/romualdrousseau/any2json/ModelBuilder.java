package com.github.romualdrousseau.any2json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.any2json.base.ModelData;

public class ModelBuilder {

    public ModelBuilder() {
        this.reset();
    }

    public ModelBuilder reset() {
        this.modelData = ModelData.empty();
        this.entities = Collections.emptyList();
        this.patterns = Collections.emptyMap();
        this.filters = Collections.emptyList();
        this.pivotEntities = Collections.emptyList();
        this.tags = Collections.emptyList();
        this.requiredTags = Collections.emptyList();
        this.tableParser = null;
        this.tagClassifier = null;
        return this;
    }

    public ModelBuilder fromModelData(final ModelData modelData) {
        this.modelData = modelData;
        this.entities = modelData.getList("entities");
        this.patterns = modelData.getMap("patterns");
        this.filters = modelData.getList("filters");
        this.pivotEntities = modelData.getList("pivotEntityList");
        this.tags = modelData.getList("tags");
        this.requiredTags = modelData.getList("requiredTags");
        return this;
    }

    public ModelBuilder fromResource(final Class<?> clazz, final String resourceName)
            throws IOException, URISyntaxException {
        return this.fromModelData(ModelData.loadFromResource(clazz, resourceName));
    }

    public ModelBuilder fromPath(final Path path) {
        return this.fromModelData(ModelData.loadFromPath(path));
    }

    public ModelBuilder fromURI(final String uri) throws IOException, InterruptedException {
        return this.fromModelData(ModelData.loadFromWebURL(uri));
    }

    public List<String> getEntityList() {
        return this.entities;
    }

    public ModelBuilder setEntityList(final List<String> entities) {
        this.entities = entities;
        return this;
    }

    public Map<String, String> getPatternMap() {
        return this.patterns;
    }

    public ModelBuilder setPatternMap(final Map<String, String> patterns) {
        this.patterns = patterns;
        return this;
    }

    public List<String> getFilters() {
        return this.filters;
    }

    public ModelBuilder setFilters(final List<String> filters) {
        this.filters = filters;
        return this;
    }

    public List<String> getPivotEntityList() {
        return this.pivotEntities;
    }

    public ModelBuilder setPivotEntityList(final List<String> pivotEntities) {
        this.pivotEntities = pivotEntities;
        return this;
    }

    public List<String> getTagList() {
        return this.tags;
    }

    public ModelBuilder setTagList(final List<String> tags) {
        this.tags = tags;
        return this;
    }

    public List<String> getRequiredTagList() {
        return this.requiredTags;
    }

    public ModelBuilder setRequiredTagList(final List<String> requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    public ModelBuilder setTableParser(final TableParser tableParser) {
        this.tableParser = tableParser;
        return this;
    }

    public ModelBuilder setTagClassifier(final TagClassifier tagClassifier) {
        this.tagClassifier = tagClassifier;
        return this;
    }

    public Model build() {
        this.updateModelData();
        final var model = new Model(this.modelData);
        if (this.tableParser != null) {
            this.tableParser.setModel(model);
        }
        if (this.tagClassifier != null) {
            this.tagClassifier.setModel(model);
        }
        return model;
    }

    private void updateModelData() {
        this.modelData.setList("entities", this.entities);
        this.modelData.setMap("patterns", this.patterns);
        this.modelData.setList("filters", this.filters);
        this.modelData.setList("pivotEntityList", this.pivotEntities);
        this.modelData.setList("tags", this.tags);
        this.modelData.setList("requiredTags", this.requiredTags);
    }

    private ModelData modelData;
    private List<String> entities;
    private Map<String, String> patterns;
    private List<String> filters;
    private List<String> pivotEntities;
    private List<String> tags;
    private List<String> requiredTags;
    private TableParser tableParser;
    private TagClassifier tagClassifier;
}
