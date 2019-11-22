package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.base.Header;

public class MetaKeyValueHeader extends Header {

    public MetaKeyValueHeader(ICell key, ICell value, ITagClassifier classifier) {
        super(classifier);
        this.key = key;
        this.value = value;
    }

    public String getName() {
        return this.key.getValue();
    }

    public String getValue() {
        return this.value.getValue();
    }

    public int getColumnIndex() {
        return -1;
    }

    private ICell key;
    private ICell value;
}
