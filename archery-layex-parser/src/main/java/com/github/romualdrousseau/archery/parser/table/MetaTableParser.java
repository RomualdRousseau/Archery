package com.github.romualdrousseau.archery.parser.table;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.MetaTable;
import com.github.romualdrousseau.archery.header.MetaHeader;
import com.github.romualdrousseau.archery.header.MetaKeyValueHeader;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public class MetaTableParser extends TableParser<BaseCell> {

    public static final int META_KEY = 1;
    public static final int META_VALUE = 2;

    public MetaTableParser(final MetaTable metaTable) {
        this.metaTable = metaTable;

    }

    public void processSymbolFunc(final BaseCell cell) {
        if (this.getColumn() == 0) {
            this.processHeader();
            this.key = null;
            this.value = null;
        }
        if (this.getGroup() == META_KEY && this.key == null) {
            this.key = cell;
        }
        if (this.getGroup() == META_VALUE && this.value == null) {
            this.value = cell;
        }
        if (cell == BaseCell.EndOfStream) {
            this.processHeader();
        }
    }

    private void processHeader() {
        final var hasKey = this.key != null && this.key.hasValue();
        final var hasValue = this.value != null && this.value.hasValue();
        if (hasKey && hasValue) {
            this.metaTable.addHeader(new MetaKeyValueHeader(this.metaTable, this.key, this.value));
        } else if (hasKey && !hasValue) {
            this.metaTable.addHeader(new MetaHeader(this.metaTable, this.key));
        } else if (!hasKey && hasValue) {
            this.metaTable.addHeader(new MetaHeader(this.metaTable, this.value));
        }
    }

    private final MetaTable metaTable;
    private BaseCell key;
    private BaseCell value;
}
