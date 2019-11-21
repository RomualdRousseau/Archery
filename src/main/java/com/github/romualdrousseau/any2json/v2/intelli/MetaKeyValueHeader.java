package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.IHeader;

public class MetaKeyValueHeader implements IHeader {

    public MetaKeyValueHeader(ICell key, ICell value, ITagClassifier classifier) {
        this.key = key;
        this.value = value;
        //this.classifier = classifier;
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
    //private ITagClassifier classifier;
}
