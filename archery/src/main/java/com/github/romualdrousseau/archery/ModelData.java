package com.github.romualdrousseau.archery;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ModelData {

    Iterable<String> keys();

    <T> Optional<T> get(String key);

    <T> ModelData set(String key, T value);

    List<String> getList(String key);

    ModelData setList(String key, List<String> values);

    Map<String, String> getMap(String key);

    ModelData setMap(String key, Map<String, String> values);

    void save(Path path);
}
