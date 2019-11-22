package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaKeyValueHeader;
import com.github.romualdrousseau.any2json.v2.layex.Context;

public class MetaTableContext extends Context<Cell> {

    public MetaTableContext(MetaTable metaTable) {
        this.metaTable = metaTable;
    }

    public void processSymbolFunc(Cell cell) {
        if (this.getColumn() == 0) {
            this.key = cell;
        } else if (this.getColumn() == 1) {
            this.value = cell;
        } else if (cell.getSymbol().equals("$")) {
            this.metaTable.addHeader(new MetaKeyValueHeader(this.key, this.value, this.metaTable.getClassifier()));
        }
    }

    private MetaTable metaTable;
    private Cell key;
    private Cell value;
}
