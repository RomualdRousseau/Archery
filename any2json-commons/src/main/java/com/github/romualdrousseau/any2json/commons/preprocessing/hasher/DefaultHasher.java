package com.github.romualdrousseau.any2json.commons.preprocessing.hasher;

import com.github.romualdrousseau.any2json.commons.preprocessing.Text;

public class DefaultHasher implements Text.IHasher {

    @Override
    public Integer apply(final String w) {
        return w.hashCode();
    }
}
