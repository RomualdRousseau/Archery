package com.github.romualdrousseau.any2json.document.xml;

import com.github.romualdrousseau.any2json.Table;

import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

class XmlTable extends Table {
    public XmlTable(Worksheet sheet, int firstColumn, int firstRow, int lastColumn,
            int lastRow, int groupId) {
        this.sheet = sheet;
        buildDataTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    public XmlTable(XmlTable parent, int firstColumn, int firstRow, int lastColumn,
            int lastRow, int groupId) {
        this.sheet = parent.sheet;
        buildMetaTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected XmlRow getInternalRowAt(int i) {
        Row row = this.sheet.getRowAt(i + 1);
        return (row != null) ? new XmlRow(this, row, this.lastGroupId) : null;
    }

    protected XmlTable createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        return new XmlTable(this, firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected Worksheet sheet;
}
