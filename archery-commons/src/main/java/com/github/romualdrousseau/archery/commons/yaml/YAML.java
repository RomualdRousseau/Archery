package com.github.romualdrousseau.archery.commons.yaml;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.romualdrousseau.archery.commons.yaml.jackson.YAMLJacksonFactory;

public class YAML {
    private static YAMLFactory Factory = new YAMLJacksonFactory();

    public static YAMLArray newArray() {
        return YAML.Factory.newArray();
    }

    public static YAMLArray arrayOf(final String data) {
        return YAML.Factory.parseArray(data);
    }

    public static YAMLArray arrayOf(final Object object) {
        return YAML.Factory.parseArray(object);
    }

    public static <T> YAMLArray arrayOf(final List<T> l) {
        final var array = YAML.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> YAMLArray arrayOf(final Stream<T> l) {
        final var array = YAML.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> YAMLArray arrayOf(final Map<String, T> m) {
        final var array = YAML.newArray();
        m.forEach((k, v) -> {
            final var pair = YAML.newObject();
            pair.set("key", k);
            pair.set("value", v);
            array.append(pair);
        });
        return array;
    }

    public static YAMLArray loadArray(final Path filePath) {
        return YAML.Factory.loadArray(filePath);
    }

    public static void saveArray(final YAMLArray a, final Path filePath) {
        YAML.Factory.saveArray(a, filePath, false);
    }

    public static void saveArray(final YAMLArray a, final Path filePath, final boolean pretty) {
        YAML.Factory.saveArray(a, filePath, pretty);
    }

    public static YAMLObject newObject() {
        return YAML.Factory.newObject();
    }

    public static YAMLObject objectOf(final String data) {
        return YAML.Factory.parseObject(data);
    }

    public static YAMLObject objectOf(final Object object) {
        return YAML.Factory.parseObject(object);
    }

    public static <T> YAMLObject objectOf(final Map<String, T> m) {
        final YAMLObject object = YAML.newObject();
        m.forEach((k, v) -> object.set(k, v));
        return object;
    }

    public static YAMLObject loadObject(final Path filePath) {
        return YAML.Factory.loadObject(filePath);
    }

    public static void saveObject(final YAMLObject o, final Path filePath) {
        YAML.Factory.saveObject(o, filePath, false);
    }

    public static void saveObject(final YAMLObject o, final Path filePath, final boolean pretty) {
        YAML.Factory.saveObject(o, filePath, pretty);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> query(final Object a, final String q) {
        Object curr = a;
        for(final var token: Arrays.asList(q.split("\\."))) {
            if (curr instanceof YAMLArray) {
                final int i = Integer.parseInt(token);
                curr = ((YAMLArray) curr).get(i).orElse(null);
            } else if (curr instanceof YAMLObject) {
                curr = ((YAMLObject) curr).get(token).orElse(null);
            } else {
                curr = null;
            }
        }
        return Optional.ofNullable((T) curr);
    }

    public static <T> Stream<T> queryStream(final Object a, final String q) {
        return YAML.<T>query(a, q)
                .filter(o -> o instanceof YAMLArray)
                .map(o -> ((YAMLArray) o).<T>stream())
                .orElse(Stream.empty());
    }
}
