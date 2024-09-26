package com.github.romualdrousseau.any2json.commons.json;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface JSONArray {

    int size();

    <T> Optional<T> get(int i);

    <T> JSONArray set(int i, T o);

    <T> JSONArray append(T o);

    JSONArray remove(int i);

    String toString(final boolean pretty);

    String toString();

    default <T> Stream<T> stream() {
        Iterable<T> it = new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private int idx = 0;

                    @Override
                    public boolean hasNext() {
                        return idx < JSONArray.this.size();
                    }

                    @Override
                    public T next() {
                        return JSONArray.this.<T>get(idx++).get();
                    }
                };
            }
        };
        return StreamSupport.stream(it.spliterator(), false);
    }
}
