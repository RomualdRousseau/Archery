package com.github.romualdrousseau.archery.header;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;

public class MetaGroupHeader extends MetaTableHeader {

    public MetaGroupHeader(final BaseTable table, final BaseCell cell) {
        super(table, cell);
    }

    @Override
    public BaseHeader clone() {
        return new MetaGroupHeader(this.getTable(), this.getCell());
    }

    @Override
    public String getName() {
        return String.format(this.getTable().getSheet().getGroupValueFormat(), super.getName());
    }
}
