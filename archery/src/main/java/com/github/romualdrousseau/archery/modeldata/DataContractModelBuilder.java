package com.github.romualdrousseau.archery.modeldata;

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

import com.github.romualdrousseau.archery.Model;
import com.github.romualdrousseau.archery.commons.yaml.YAML;
import com.github.romualdrousseau.archery.commons.yaml.YAMLObject;

public class DataContractModelBuilder {

    public DataContractModelBuilder() {
        this.reset();
    }

    public DataContractModelBuilder reset() {
        this.modelData = DataContractModelData.empty();
        this.lexicon = Collections.emptyList();
        return this;
    }

    public DataContractModelBuilder fromModelData(final DataContractModelData modelData) {
        this.modelData = modelData;
        return this;
    }

    public DataContractModelBuilder fromYAML(final YAMLObject yamlObject) {
        return this.fromModelData(new DataContractModelData(yamlObject));
    }

    public DataContractModelBuilder fromResource(final Class<?> clazz, final String resourceName)
            throws IOException, URISyntaxException {
        final URL resourceUrl = clazz.getResource(resourceName);
        if (resourceUrl == null) {
            throw new IOException("Error loading model");
        }
        return this.fromModelData(new DataContractModelData(YAML.loadObject(Path.of(resourceUrl.toURI()))));
    }

    public DataContractModelBuilder fromPath(final Path path) {
        return this.fromModelData(new DataContractModelData(YAML.loadObject(path)));
    }

    public DataContractModelBuilder fromURL(final String url) throws IOException, InterruptedException {
        final var client = HttpClient.newHttpClient();
        final var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Error loading model");
        }
        return this.fromModelData(new DataContractModelData(YAML.objectOf(response.body())));
    }

    public DataContractModelBuilder setLexicon(final String lexicon) throws IOException, URISyntaxException {
        final URL resourceUrl = this.getClass().getResource("/lexicon/" + lexicon + ".json");
        if (resourceUrl == null) {
            throw new IOException("Error loading lexicon");
        }
        this.lexicon = YAML.loadArray(Path.of(resourceUrl.toURI())).<String>stream().toList();
        return this;
    }

    public Model build() {
        this.updateModelData();
        return new Model(this.modelData);
    }

    private void updateModelData() {
        this.modelData.setList("lexicon", this.lexicon);
    }

    private DataContractModelData modelData;
    private List<String> lexicon;
}
