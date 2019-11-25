package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;

public class MetaTableHeader extends MetaHeader {

    public MetaTableHeader(final IntelliTable table, final BaseCell cell) {
        super(table, cell);
    }

    public MetaTableHeader(final MetaTableHeader parent) {
        super((IntelliTable) parent.getTable(), parent.getCell());
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public BaseCell getCellForRow(final BaseRow row) {
        return this.getCell();
    }

    @Override
    public AbstractHeader clone() {
        return new MetaTableHeader(this);
    }
}
