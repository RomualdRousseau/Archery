package com.github.romualdrousseau.archery.commons.dsf.yaml;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFFactory;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;
import com.github.romualdrousseau.archery.commons.dsf.yaml.jackson.YAMLJacksonFactory;

public class YAML {
    private static DSFFactory Factory = new YAMLJacksonFactory();

    public static DSFArray newArray() {
        return YAML.Factory.newArray();
    }

    public static DSFArray arrayOf(final String data) {
        return YAML.Factory.parseArray(data);
    }

    public static DSFArray arrayOf(final Object object) {
        return YAML.Factory.parseArray(object);
    }

    public static <T> DSFArray arrayOf(final List<T> l) {
        final var array = YAML.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> DSFArray arrayOf(final Stream<T> l) {
        final var array = YAML.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> DSFArray arrayOf(final Map<String, T> m) {
        final var array = YAML.newArray();
        m.forEach((k, v) -> {
            final var pair = YAML.newObject();
            pair.set("key", k);
            pair.set("value", v);
            array.append(pair);
        });
        return array;
    }

    public static DSFArray loadArray(final Path filePath) {
        return YAML.Factory.loadArray(filePath);
    }

    public static void saveArray(final DSFArray a, final Path filePath) {
        YAML.Factory.saveArray(a, filePath, false);
    }

    public static void saveArray(final DSFArray a, final Path filePath, final boolean pretty) {
        YAML.Factory.saveArray(a, filePath, pretty);
    }

    public static DSFObject newObject() {
        return YAML.Factory.newObject();
    }

    public static DSFObject objectOf(final String data) {
        return YAML.Factory.parseObject(data);
    }

    public static DSFObject objectOf(final Object object) {
        return YAML.Factory.parseObject(object);
    }

    public static <T> DSFObject objectOf(final Map<String, T> m) {
        final var object = YAML.newObject();
        m.forEach((k, v) -> object.set(k, v));
        return object;
    }

    public static DSFObject loadObject(final Path filePath) {
        return YAML.Factory.loadObject(filePath);
    }

    public static void saveObject(final DSFObject o, final Path filePath) {
        YAML.Factory.saveObject(o, filePath, false);
    }

    public static void saveObject(final DSFObject o, final Path filePath, final boolean pretty) {
        YAML.Factory.saveObject(o, filePath, pretty);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> query(final Object a, final String q) {
        Object curr = a;
        for(final var token: Arrays.asList(q.split("\\."))) {
            if (curr instanceof DSFArray) {
                final int i = Integer.parseInt(token);
                curr = ((DSFArray) curr).get(i).orElse(null);
            } else if (curr instanceof DSFObject) {
                curr = ((DSFObject) curr).get(token).orElse(null);
            } else {
                curr = null;
            }
        }
        return Optional.ofNullable((T) curr);
    }

    public static <T> Stream<T> queryStream(final Object a, final String q) {
        return YAML.<T>query(a, q)
                .filter(o -> o instanceof DSFArray)
                .map(o -> ((DSFArray) o).<T>stream())
                .orElse(Stream.empty());
    }
}
