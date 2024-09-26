package com.github.romualdrousseau.any2json.commons.json;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JSONCollector {

    public static <T> Collector<JSONObject, ?, Map<String, T>> toMap(final String key, final String value) {
        return Collectors.toMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }

    public static <T> Collector<JSONObject, ?, Map<String, T>> toUnmodifiableMap(final String key, final String value) {
        return Collectors.toUnmodifiableMap(x -> x.<String>get(key).get(), x -> x.<T>get(value).get());
    }
}
