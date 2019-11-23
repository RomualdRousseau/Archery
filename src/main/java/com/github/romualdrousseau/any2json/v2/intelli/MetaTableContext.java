package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaKeyValueHeader;
import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class MetaTableContext extends Context<AbstractCell> {

    public MetaTableContext(MetaTable metaTable) {
        this.metaTable = metaTable;
    }

    public void processSymbolFunc(AbstractCell cell) {
        if (this.getColumn() == 0) {
            this.key = cell;
        } else if (this.getColumn() == 1) {
            this.value = cell;
        } else if (cell.getSymbol().equals("$")) {
            if (StringUtility.isEmpty(this.value.getValue())) {
                this.metaTable.addHeader(new MetaHeader(this.key));
            } else {
                this.metaTable.addHeader(new MetaKeyValueHeader(this.key, this.value));
            }
        }
    }

    private MetaTable metaTable;
    private AbstractCell key;
    private AbstractCell value;
}
