package com.github.romualdrousseau.archery.parser.table;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.MetaTable;
import com.github.romualdrousseau.archery.header.MetaHeader;
import com.github.romualdrousseau.archery.header.MetaKeyValueHeader;
import com.github.romualdrousseau.archery.layex.TableParser;

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
