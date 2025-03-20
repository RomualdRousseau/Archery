package com.github.romualdrousseau.archery.modeldata;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.romualdrousseau.archery.ModelData;

public class EmptyModelData implements ModelData {

    public static EmptyModelData empty() {
        return new EmptyModelData();
    }

    @Override
    public Iterable<String> keys() {
        return Collections.emptyList();
    }

    @Override
    public <T> Optional<T> get(String key) {
        return Optional.empty();
    }

    @Override
    public <T> ModelData set(String key, T value) {
        return this;
    }

    @Override
    public List<String> getList(final String key) {
        return Collections.emptyList();
    }

    @Override
    public ModelData setList(final String key, final List<String> values) {
        return this;
    }

    @Override
    public Map<String, String> getMap(final String key) {
        return Collections.emptyMap();
    }

    @Override
    public ModelData setMap(final String key, final Map<String, String> values) {
        return this;
    }

    @Override
    public void save(final Path path) {
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
}
