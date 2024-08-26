package com.github.romualdrousseau.any2json.base;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;

public class ModelData {

    public static ModelData empty() {
        return new ModelData(JSON.newObject());
    }

    public static ModelData loadFromJSON(final JSONObject jsonObject) {
        return new ModelData(jsonObject);
    }

    public static ModelData loadFromResource(final Class<?> clazz, final String resourceName)
            throws IOException, URISyntaxException {
        final URL resourceUrl = clazz.getResource(resourceName);
        if (resourceUrl == null) {
            throw new IOException("Error loading model");
        }
        return new ModelData(JSON.loadObject(Path.of(resourceUrl.toURI())));
    }

    public static ModelData loadFromPath(final Path path) {
        return new ModelData(JSON.loadObject(path));
    }

    public static ModelData loadFromWebURL(final String url) throws IOException, InterruptedException {
        final var client = HttpClient.newHttpClient();
        final var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Error loading model");
        }
        return new ModelData(JSON.objectOf(response.body()));
    }

    private ModelData(final JSONObject backstore) {
        this.backstore = backstore;
    }

    public boolean hasKey(final String key) {
        return this.backstore.get(key).isPresent();
    }

    public int getInt(final String key) {
        return this.backstore.getInt(key);
    }

    public void setInt(final String key, final int value) {
        this.backstore.setInt(key, value);
    }

    public void setString(final String key, final String value) {
        this.backstore.setString(key, value);
    }

    public String getString(final String key) {
        return this.backstore.getString(key);
    }

    public List<String> getList(final String key) {
        return JSON.<String>streamOf(this.backstore.getArray(key)).collect(Collectors.toUnmodifiableList());
    }

    public List<String> getMutableList(final String key) {
        return JSON.<String>streamOf(this.backstore.getArray(key)).collect(Collectors.toList());
    }

    public void setList(final String key, final List<String> values) {
        this.backstore.setArray(key, JSON.arrayOf(values));
    }

    public Map<String, String> getMap(final String key) {
        return JSON.<JSONObject>streamOf(this.backstore.getArray(key))
                .collect(Collectors.toUnmodifiableMap(x -> x.getString("key"), x -> x.getString("value")));
    }

    public Map<String, String> getMutableMap(final String key) {
        return JSON.<JSONObject>streamOf(this.backstore.getArray(key))
                .collect(Collectors.toMap(x -> x.getString("key"), x -> x.getString("value")));
    }

    public void setMap(final String key, final Map<String, String> values) {
        this.backstore.setArray(key, JSON.arrayOf(values));
    }

    public void save(final Path path) {
        JSON.saveObject(this.backstore, path);
    }

    private final JSONObject backstore;
}
