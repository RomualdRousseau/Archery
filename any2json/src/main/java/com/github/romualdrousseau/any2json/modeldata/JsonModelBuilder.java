package com.github.romualdrousseau.any2json.modeldata;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.TableParser;
import com.github.romualdrousseau.any2json.TagClassifier;
import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.shuju.yaml.YAML;

public class JsonModelBuilder {

    public JsonModelBuilder() {
        this.reset();
    }

    public JsonModelBuilder reset() {
        this.modelData = JsonModelData.empty();
        this.entities = Collections.emptyList();
        this.patterns = Collections.emptyMap();
        this.filters = Collections.emptyList();
        this.pivotEntities = Collections.emptyList();
        this.tags = Collections.emptyList();
        this.requiredTags = Collections.emptyList();
        this.lexicon = Collections.emptyList();
        this.tableParser = null;
        this.tagClassifier = null;
        return this;
    }

    public JsonModelBuilder fromModelData(final JsonModelData modelData) {
        this.modelData = modelData;
        this.entities = modelData.getList("entities");
        this.patterns = modelData.getMap("patterns");
        this.filters = modelData.getList("filters");
        this.pivotEntities = modelData.getList("pivotEntityList");
        this.tags = modelData.getList("tags");
        this.requiredTags = modelData.getList("requiredTags");
        this.lexicon = modelData.getList("lexicon");
        return this;
    }

    public JsonModelBuilder fromJSON(final JSONObject jsonObject) {
        return this.fromModelData(new JsonModelData(jsonObject));
    }

    public JsonModelBuilder fromResource(final Class<?> clazz, final String resourceName)
            throws IOException, URISyntaxException {
        final URL resourceUrl = clazz.getResource(resourceName);
        if (resourceUrl == null) {
            throw new IOException("Error loading model");
        }
        return this.fromModelData(new JsonModelData(JSON.loadObject(Path.of(resourceUrl.toURI()))));
    }

    public JsonModelBuilder fromPath(final Path path) {
        return this.fromModelData(new JsonModelData(JSON.loadObject(path)));
    }

    public JsonModelBuilder fromURL(final String url) throws IOException, InterruptedException {
        final var client = HttpClient.newHttpClient();
        final var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Error loading model");
        }
        return this.fromModelData(new JsonModelData(JSON.objectOf(response.body())));
    }

    public List<String> getEntityList() {
        return this.entities;
    }

    public JsonModelBuilder setEntityList(final List<String> entities) {
        this.entities = entities;
        return this;
    }

    public Map<String, String> getPatternMap() {
        return this.patterns;
    }

    public JsonModelBuilder setPatternMap(final Map<String, String> patterns) {
        this.patterns = patterns;
        return this;
    }

    public JsonModelBuilder setFilters(final List<String> filters) {
        this.filters = filters;
        return this;
    }

    public JsonModelBuilder setPivotEntityList(final List<String> pivotEntities) {
        this.pivotEntities = pivotEntities;
        return this;
    }

    public JsonModelBuilder setTagList(final List<String> tags) {
        this.tags = tags;
        return this;
    }

    public JsonModelBuilder setRequiredTagList(final List<String> requiredTags) {
        this.requiredTags = requiredTags;
        return this;
    }

    public JsonModelBuilder setTableParser(final TableParser tableParser) {
        this.tableParser = tableParser;
        return this;
    }

    public JsonModelBuilder setTagClassifier(final TagClassifier tagClassifier) {
        this.tagClassifier = tagClassifier;
        return this;
    }

    public JsonModelBuilder setLexicon(final String lexicon) throws IOException, URISyntaxException {
        final URL resourceUrl = this.getClass().getResource("/lexicon/" + lexicon + ".json");
        if (resourceUrl == null) {
            throw new IOException("Error loading lexicon");
        }
        this.lexicon = YAML.loadArray(Path.of(resourceUrl.toURI())).<String>stream().toList();
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
        this.modelData.setList("lexicon", this.lexicon);
    }

    private JsonModelData modelData;
    private List<String> entities;
    private Map<String, String> patterns;
    private List<String> filters;
    private List<String> pivotEntities;
    private List<String> tags;
    private List<String> requiredTags;
    private TableParser tableParser;
    private TagClassifier tagClassifier;
    private List<String> lexicon;
}
