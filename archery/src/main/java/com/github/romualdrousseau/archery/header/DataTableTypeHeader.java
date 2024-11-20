package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;

public class DataTableTypeHeader extends DataTableHeader {

    public DataTableTypeHeader(final BaseHeader parent) {
        super(parent);
    }

    public DataTableTypeHeader(final BaseTable table, final BaseCell cell) {
        super(table, cell);
    }

    @Override
    public BaseHeader clone() {
        return new DataTableTypeHeader(this);
    }
}
