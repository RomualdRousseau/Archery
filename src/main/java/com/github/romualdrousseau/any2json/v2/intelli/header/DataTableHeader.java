package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;

public class DataTableHeader extends AbstractHeader {

    public DataTableHeader(final IntelliTable table, final BaseCell cell) {
        super(table, cell);
        assert(table.getClassifier() != null) : "Classifier must be defined";
    }

    private DataTableHeader(final DataTableHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public BaseCell getCellForRow(final BaseRow row) {
        return row.getCellAt(this.getColumnIndex());
    }

    @Override
    public AbstractHeader clone() {
        return new DataTableHeader(this);
    }

    @Override
    public boolean hasTag() {
        return false;
    }

    @Override
    public HeaderTag getTag() {
        return null;
    }

    @Override
    public Vector getEntityVector() {
        return null;
    }

    @Override
    public DataRow buildTrainingRow(final String tagValue, final Header[] conflicts, final boolean ensureWordsExists) {
        return null;
    }

    private String name;
}
