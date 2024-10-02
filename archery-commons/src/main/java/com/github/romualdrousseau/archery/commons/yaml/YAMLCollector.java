package com.github.romualdrousseau.archery.commons.yaml;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class YAMLCollector {

    public static <T> Collector<YAMLObject, ?, Map<String, T>> toMap(final String key, final String value) {
        return Collectors.toMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }

    public static <T> Collector<YAMLObject, ?, Map<String, T>> toUnmodifiableMap(final String key, final String value) {
        return Collectors.toUnmodifiableMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }
}
