package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaKeyValueHeader;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class MetaTableContext extends Context<AbstractCell> {

    public MetaTableContext(final MetaTable metaTable) {
        this.metaTable = metaTable;
    }

    public void processSymbolFunc(final AbstractCell cell) {
        if (this.getColumn() == 0) {
            this.key = cell;
        } else if (this.getColumn() == 1) {
            this.value = cell;
        } else if (cell.getSymbol().equals("$")) {
            if (!this.value.hasValue()) {
                this.metaTable.addHeader(new MetaHeader(this.metaTable, this.key));
            } else {
                this.metaTable.addHeader(new MetaKeyValueHeader(this.metaTable, this.key, this.value));
            }
        }
    }

    private final MetaTable metaTable;
    private AbstractCell key;
    private AbstractCell value;
}
