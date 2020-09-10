package com.github.romualdrousseau.any2json.intelli;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.intelli.header.MetaKeyValueHeader;

public class MetaTableContext extends Context<BaseCell> {

    public MetaTableContext(final MetaTable metaTable) {
        this.metaTable = metaTable;
    }

    public void processSymbolFunc(final BaseCell cell) {
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
    private BaseCell key;
    private BaseCell value;
}
