package com.github.romualdrousseau.any2json.intelli;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.intelli.header.DataTableHeader;
import com.github.romualdrousseau.any2json.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.layex.TableLexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class DataTable extends CompositeTable {

    public DataTable(CompositeTable table) {
        super(table);
        this.dataTableParser = null;
        this.buildIntelliTable();
    }

    public DataTable(CompositeTable table, TableMatcher layex, int rowOffset, DataTableParserFactory dataTableParserFactory) {
        super(table);
        this.dataTableParser = dataTableParserFactory.getInstance(this);
        layex.match(new TableLexer(table, rowOffset), this.dataTableParser);
        if (this.dataTableParser.getSplitRows().size() > 0) {
            this.adjustLastRow(table.getFirstRow() + this.dataTableParser.getSplitRows().get(0) - 1, true);
        }
        if (rowOffset > 0) {
            this.setFirstRowOffset(this.getFirstRowOffset() + rowOffset);
        }
        this.ignoreRows().clear();
        this.ignoreRows().addAll(this.dataTableParser.getIgnoreRows());
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

    public DataTableParser getDataTableParser() {
		return this.dataTableParser;
    }

    private void buildIntelliTable() {
        for (Cell cell : this.getRowAt(0).cells()) {
            BaseCell baseCell = (BaseCell) cell;
            if(baseCell.isPivotHeader()) {
                PivotKeyHeader foundPivot = this.findFirstPivotHeader();
                if (foundPivot == null) {
                    this.addHeader(new PivotKeyHeader(this, baseCell));
                } else {
                    foundPivot.addEntry(baseCell);
                }
            } else {
                this.addHeader(new DataTableHeader(this, baseCell));
            }
        }
        this.setFirstRowOffset(1);
    }

    private final LinkedList<RowGroup> rowGroups = new LinkedList<RowGroup>();
    private final DataTableParser dataTableParser;
}
