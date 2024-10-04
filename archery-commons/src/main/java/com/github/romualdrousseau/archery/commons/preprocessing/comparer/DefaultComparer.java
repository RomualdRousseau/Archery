package com.github.romualdrousseau.archery.commons.preprocessing.comparer;

import java.util.List;
import java.util.Optional;

import com.github.romualdrousseau.archery.commons.preprocessing.Text;

public class DefaultComparer implements Text.IComparer {

    @Override
    public Boolean apply(final String a, final List<String> b) {
        return b.contains(a);
    }

    @Override
    public String anonymize(final String v) {
        return v;
    }

    @Override
    public String anonymize(final String v, final String pattern) {
        return v;
    }

    @Override
    public Optional<String> find(final String v) {
        return Optional.empty();
    }

    @Override
    public Optional<String> find(final String v, final String pattern) {
        return Optional.empty();
    }
}
