package com.github.romualdrousseau.any2json.commons.yaml;

import java.util.Optional;

public interface YAMLObject {

    Iterable<String> keys();

    <T> Optional<T> get(String k);

    <T> YAMLObject set(String k, T o);

    YAMLObject remove(String k);

    String toString(final boolean pretty);

    String toString();
}
