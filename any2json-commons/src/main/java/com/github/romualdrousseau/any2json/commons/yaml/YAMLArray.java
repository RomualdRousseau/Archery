package com.github.romualdrousseau.any2json.commons.yaml;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface YAMLArray {

    int size();

    <T> Optional<T> get(int i);

    <T> YAMLArray set(int i, T o);

    <T> YAMLArray append(T o);

    YAMLArray remove(int i);

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
                        return idx < YAMLArray.this.size();
                    }

                    @Override
                    public T next() {
                        return YAMLArray.this.<T>get(idx++).get();
                    }
                };
            }
        };
        return StreamSupport.stream(it.spliterator(), false);
    }
}
