package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.RowGroup;
import com.github.romualdrousseau.any2json.v2.intelli.header.IntelliHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.util.TableLexer;

public class DataTable extends CompositeTable {

    public DataTable(CompositeTable table) {
        super(table);
        this.buildIntelliTable(table);
        this.updateHeaderTags();
        this.setLoadCompleted(true);
    }

    public DataTable(CompositeTable table, LayexMatcher layex) {
        super(table);
        layex.match(new TableLexer(table), new DataTableContext(this));
        this.setLoadCompleted(true);
    }

    public int getNumberOfRowGroups() {
        return this.rowGroups.size();
    }

    public Iterable<RowGroup> rowGroups() {
        return this.rowGroups;
    }

    public void addRowGroup(RowGroup rowGroup) {
        this.rowGroups.add(rowGroup);
    }

    public MetaTableHeader findFirstMetaTableHeader() {
        MetaTableHeader result = null;
        for (final Header header : this.headers()) {
            if (header instanceof MetaTableHeader) {
                result = (MetaTableHeader) header;
                break;
            }
        }
		return result;
	}

    public PivotKeyHeader findFirstPivotHeader() {
        PivotKeyHeader result = null;
        for (final Header header : this.headers()) {
            if (header instanceof PivotKeyHeader) {
                result = (PivotKeyHeader) header;
                break;
            }
        }
		return result;
	}

    private void buildIntelliTable(CompositeTable table) {
        for (Cell cell : table.getRowAt(0).cells()) {
            this.addHeader(new IntelliHeader(this, (BaseCell) cell));
        }
        this.setFirstRowOffset(1);
    }

    private final LinkedList<RowGroup> rowGroups = new LinkedList<RowGroup>();
}
