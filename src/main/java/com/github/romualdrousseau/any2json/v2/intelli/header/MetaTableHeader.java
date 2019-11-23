package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;

public class MetaTableHeader extends MetaHeader {

    public MetaTableHeader(AbstractCell cell) {
        super(cell);
    }

    @Override
    public AbstractHeader clone() {
        return new MetaTableHeader(this.getCell());
    }
}
