package com.github.romualdrousseau.archery.commons.types;

import java.util.Map;

public class Pair implements Map.Entry<String, String> {
    private final String left;
    private final String right;

    public Pair(final String left, final String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getKey() {
        return this.left;
    }

    @Override
    public String getValue() {
        return this.right;
    }

    @Override
    public String setValue(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'setValue'");
    }
}
