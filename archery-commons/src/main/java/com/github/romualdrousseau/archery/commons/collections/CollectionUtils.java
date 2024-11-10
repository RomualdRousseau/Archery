package com.github.romualdrousseau.archery.commons.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static List<Integer> mutableRange(int a, int b) {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = a; i < b; i++) {
            result.add(i);
        }
        return result;
    }

    public static <T> List<T> shuffle(List<T> l) {
        Collections.shuffle(l);
        return l;
    }

    public static <T> Map<String, T> sortMap(final Map<String, T> map, final List<String> list) {
        final var sortedMap = new LinkedHashMap<String, T>();
        list.forEach(x -> {
            map.forEach((k, v) -> {
                if (x.equals(v)) {
                    sortedMap.put(k, v);
                }
            });
        });
        return sortedMap;
    }
}
