package com.github.romualdrousseau.archery.commons.dsf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DSFCollector {

    public static <T> Collector<DSFObject, ?, Map<String, T>> toMap(final String key, final String value) {
        return Collectors.toMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }

    public static <T> Collector<DSFObject, ?, Map<String, T>> toUnmodifiableMap(final String key, final String value) {
        return Collectors.toUnmodifiableMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }

    public static <T> Collector<DSFObject, ?, Map<String, T>> toLinkedMap(final String key, final String value) {
        return Collectors.toMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get(), (e1, e2) -> {
            throw new RuntimeException();
        }, LinkedHashMap::new);
    }
}
