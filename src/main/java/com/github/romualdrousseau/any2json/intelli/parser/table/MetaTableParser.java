package com.github.romualdrousseau.any2json.intelli.parser.table;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.intelli.MetaTable;
import com.github.romualdrousseau.any2json.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.intelli.header.MetaKeyValueHeader;
import com.github.romualdrousseau.any2json.layex.TableParser;

public class MetaTableParser extends TableParser<BaseCell> {

    public MetaTableParser(final MetaTable metaTable) {
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
