package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.IHeader;

public abstract class Header implements IHeader {

    public Header(ITagClassifier classifier) {
        this.classifier = classifier;
    }

    public boolean equals(Header o) {
        return this.getName().equals(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Header && this.equals((Header) o);
    }

    protected ITagClassifier classifier;
}
