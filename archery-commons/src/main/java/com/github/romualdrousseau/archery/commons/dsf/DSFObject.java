package com.github.romualdrousseau.archery.commons.dsf;

import java.util.Optional;

public interface DSFObject {

    Iterable<String> keys();

    <T> Optional<T> get(String k);

    <T> DSFObject set(String k, T o);

    DSFObject remove(String k);

    String toString(final boolean pretty);

    String toString();
}
