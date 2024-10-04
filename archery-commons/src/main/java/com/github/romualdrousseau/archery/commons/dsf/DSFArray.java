package com.github.romualdrousseau.archery.commons.dsf;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface DSFArray {

    int size();

    <T> Optional<T> get(int i);

    <T> DSFArray set(int i, T o);

    <T> DSFArray append(T o);

    DSFArray remove(int i);

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
                        return idx < DSFArray.this.size();
                    }

                    @Override
                    public T next() {
                        return DSFArray.this.<T>get(idx++).get();
                    }
                };
            }
        };
        return StreamSupport.stream(it.spliterator(), false);
    }
}
