package com.github.romualdrousseau.archery.commons.preprocessing.hasher;

import com.github.romualdrousseau.archery.commons.preprocessing.Text;

public class DefaultHasher implements Text.IHasher {

    @Override
    public Integer apply(final String w) {
        return w.hashCode();
    }
}
