package com.github.romualdrousseau.any2json.commons.json;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.romualdrousseau.any2json.commons.json.jackson.JSONJacksonFactory;

public class JSON {
    private static JSONFactory Factory = new JSONJacksonFactory();

    public static JSONArray newArray() {
        return JSON.Factory.newArray();
    }

    public static JSONArray arrayOf(String data) {
        return JSON.Factory.parseArray(data);
    }

    public static JSONArray arrayOf(Object object) {
        return JSON.Factory.parseArray(object);
    }

    public static <T> JSONArray arrayOf(final List<T> l) {
        final JSONArray array = JSON.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> JSONArray arrayOf(final Stream<T> l) {
        final JSONArray array = JSON.newArray();
        l.forEach(s -> array.append(s));
        return array;
    }

    public static <T> JSONArray arrayOf(final Map<String, T> m) {
        final JSONArray array = JSON.newArray();
        m.forEach((k, v) -> {
            JSONObject pair = JSON.newObject();
            pair.set("key", k);
            pair.set("value", v);
            array.append(pair);
        });
        return array;
    }

    public static JSONArray loadArray(Path filePath) {
        return JSON.Factory.loadArray(filePath);
    }

    public static void saveArray(JSONArray a, Path filePath) {
        JSON.Factory.saveArray(a, filePath, false);
    }

    public static void saveArray(JSONArray a, Path filePath, final boolean pretty) {
        JSON.Factory.saveArray(a, filePath, pretty);
    }

    public static JSONObject newObject() {
        return JSON.Factory.newObject();
    }

    public static JSONObject objectOf(String data) {
        return JSON.Factory.parseObject(data);
    }

    public static JSONObject objectOf(Object object) {
        return JSON.Factory.parseObject(object);
    }

    public static <T> JSONObject objectOf(final Map<String, T> m) {
        final JSONObject object = JSON.newObject();
        m.forEach((k, v) -> object.set(k, v));
        return object;
    }

    public static JSONObject loadObject(Path filePath) {
        return JSON.Factory.loadObject(filePath);
    }

    public static void saveObject(JSONObject o, Path filePath) {
        JSON.Factory.saveObject(o, filePath, false);
    }

    public static void saveObject(JSONObject o, Path filePath, final boolean pretty) {
        JSON.Factory.saveObject(o, filePath, pretty);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> query(final Object a, final String q) {
        Object curr = a;
        for(String token: Arrays.asList(q.split("\\."))) {
            if (curr instanceof JSONArray) {
                int i = Integer.parseInt(token);
                curr = ((JSONArray) curr).get(i).orElse(null);
            } else if (curr instanceof JSONObject) {
                curr = ((JSONObject) curr).get(token).orElse(null);
            } else {
                curr = null;
            }
        }
        return Optional.ofNullable((T) curr);
    }

    public static <T> Stream<T> queryStream(final Object a, final String q) {
        return JSON.<T>query(a, q)
                .filter(o -> o instanceof JSONArray)
                .map(o -> ((JSONArray) o).<T>stream())
                .orElse(Stream.empty());
    }
}
