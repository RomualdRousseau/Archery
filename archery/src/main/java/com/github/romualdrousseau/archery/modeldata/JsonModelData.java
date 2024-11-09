package com.github.romualdrousseau.archery.modeldata;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.romualdrousseau.archery.ModelData;
import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFCollector;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;
import com.github.romualdrousseau.archery.commons.dsf.json.JSON;

public class JsonModelData implements ModelData {

    public static JsonModelData empty() {
        return new JsonModelData(JSON.newObject());
    }

    public JsonModelData(final DSFObject backstore) {
        this.backstore = backstore;
    }

    @Override
    public <T> Optional<T> get(final String key) {
        return this.backstore.<T>get(key);
    }

    @Override
    public <T> ModelData set(final String key, final T value) {
        this.backstore.set(key, value);
        return this;
    }

    @Override
    public List<String> getList(final String key) {
        return this.backstore.<DSFArray>get(key)
                .map(x -> x.<String>stream().toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public ModelData setList(final String key, final List<String> values) {
        this.backstore.set(key, JSON.arrayOf(values));
        return this;
    }

    @Override
    public Map<String, String> getMap(final String key) {
        return this.backstore.<DSFArray>get(key)
                .map(x -> x.<DSFObject>stream().collect(DSFCollector.<String>toLinkedMap("key", "value")))
                .orElse(Collections.emptyMap());
    }

    @Override
    public ModelData setMap(final String key, final Map<String, String> values) {
        this.backstore.set(key, JSON.arrayOf(values));
        return this;
    }

    @Override
    public void save(final Path path) {
        JSON.saveObject(this.backstore, path);
    }

    private final DSFObject backstore;
}
