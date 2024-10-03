package com.github.romualdrousseau.archery.commons.dsf.json;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.romualdrousseau.archery.commons.dsf.DSFArray;
import com.github.romualdrousseau.archery.commons.dsf.DSFFactory;
import com.github.romualdrousseau.archery.commons.dsf.DSFObject;
import com.github.romualdrousseau.archery.commons.dsf.json.jackson.JSONJacksonFactory;

public class JSON {
    private static DSFFactory Factory = new JSONJacksonFactory();

    public static DSFArray newArray() {
        return JSON.Factory.newArray();
    }

    public static DSFArray arrayOf(String data) {
        return JSON.Factory.parseArray(data);
    }

    public static DSFArray arrayOf(Object object) {
        return JSON.Factory.parseArray(object);
    }

    public static <T> DSFArray arrayOf(final List<T> l) {
        final DSFArray array = JSON.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> DSFArray arrayOf(final Stream<T> l) {
        final DSFArray array = JSON.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> DSFArray arrayOf(final Map<String, T> m) {
        final DSFArray array = JSON.newArray();
        m.forEach((k, v) -> {
            DSFObject pair = JSON.newObject();
            pair.set("key", k);
            pair.set("value", v);
            array.append(pair);
        });
        return array;
    }

    public static DSFArray loadArray(Path filePath) {
        return JSON.Factory.loadArray(filePath);
    }

    public static void saveArray(DSFArray a, Path filePath) {
        JSON.Factory.saveArray(a, filePath, false);
    }

    public static void saveArray(DSFArray a, Path filePath, final boolean pretty) {
        JSON.Factory.saveArray(a, filePath, pretty);
    }

    public static DSFObject newObject() {
        return JSON.Factory.newObject();
    }

    public static DSFObject objectOf(String data) {
        return JSON.Factory.parseObject(data);
    }

    public static DSFObject objectOf(Object object) {
        return JSON.Factory.parseObject(object);
    }

    public static <T> DSFObject objectOf(final Map<String, T> m) {
        final DSFObject object = JSON.newObject();
        m.forEach((k, v) -> object.set(k, v));
        return object;
    }

    public static DSFObject loadObject(Path filePath) {
        return JSON.Factory.loadObject(filePath);
    }

    public static void saveObject(DSFObject o, Path filePath) {
        JSON.Factory.saveObject(o, filePath, false);
    }

    public static void saveObject(DSFObject o, Path filePath, final boolean pretty) {
        JSON.Factory.saveObject(o, filePath, pretty);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> query(final Object a, final String q) {
        Object curr = a;
        for(String token: Arrays.asList(q.split("\\."))) {
            if (curr instanceof DSFArray) {
                int i = Integer.parseInt(token);
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
        return JSON.<T>query(a, q)
                .filter(o -> o instanceof DSFArray)
                .map(o -> ((DSFArray) o).<T>stream())
                .orElse(Stream.empty());
    }
}
