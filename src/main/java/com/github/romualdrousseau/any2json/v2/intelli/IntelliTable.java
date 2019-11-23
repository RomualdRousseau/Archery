package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaTableHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotTableHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable extends AbstractTable {

    public IntelliTable(TableGraph root, ITagClassifier classifier) {
        super(classifier);
        this.buildHeaders(root);
        this.buildRows(root);
    }

    @Override
    public int getNumberOfColumns() {
        return this.getNumberOfHeaders();
    }

    @Override
    public int getNumberOfRows() {
        return this.rows.size();
    }

    @Override
    public AbstractRow getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        return this.rows.get(rowIndex);
    }

    private void buildHeaders(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            for (Header header : child.getTable().headers()) {
                AbstractHeader abstractHeader = (AbstractHeader) header;
                if (this.checkIfHeaderExists(abstractHeader)) {
                    continue;
                }

                AbstractHeader newHeader = abstractHeader.clone();
                newHeader.setColumnIndex(this.getNumberOfHeaders());
                this.addHeader(newHeader);

                if (header instanceof PivotTableHeader) {
                    newHeader = ((PivotTableHeader) abstractHeader).getValueHeader();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                }
            }
        }

        for (TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private void buildRows(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            if (child.getTable() instanceof DataTable) {
                this.buildRowsForOneTable((DataTable) child.getTable());
            }
        }

        for (TableGraph child : graph.children()) {
            buildRows(child);
        }
    }

    private void buildRowsForOneTable(DataTable orgTable) {
        for (Row orgRow : orgTable.rows()) {
            ArrayList<IntelliRow> newRows = buildFromOneRow(orgTable, (AbstractRow) orgRow);
            this.rows.addAll(newRows);
        }
    }

    private ArrayList<IntelliRow> buildFromOneRow(DataTable orgTable, AbstractRow orgRow) {
        ArrayList<IntelliRow> newRows = new ArrayList<IntelliRow>();

        PivotTableHeader foundPivot = null;
        for (Header header : this.headers()) {
            if (header instanceof PivotTableHeader) {
                foundPivot = (PivotTableHeader) header;
            }
        }

        if (foundPivot == null) {
            newRows.add(buildOneRow(orgTable, orgRow, null));
        } else {
            for (PivotTableHeader.PivotEntry e : foundPivot.getEntries()) {
                newRows.add(buildOneRow(orgTable, orgRow, e));
            }
        }

        return newRows;
    }

    private IntelliRow buildOneRow(DataTable orgTable, AbstractRow orgRow, PivotTableHeader.PivotEntry e) {
        IntelliRow newRow = new IntelliRow(this);

        for (Header header : this.headers()) {
            AbstractHeader abstractHeader = orgTable.findHeader((AbstractHeader) header);

            if (header instanceof TaggedHeader) {
                if(abstractHeader != null) {
                    newRow.addCell(orgRow.getCellAt(abstractHeader.getColumnIndex()));
                } else {
                    newRow.addEmptyCell();
                }
            } else if (header instanceof PivotTableHeader) {
                if(((PivotTableHeader) header).isPivotalKey()) {
                    if(abstractHeader != null && e != null) {
                        newRow.addStringCell(e.getValue());
                        newRow.addCell(orgRow.getCellAt(e.getColumnIndex()));
                    } else {
                        newRow.addEmptyCell();
                        newRow.addEmptyCell();
                    }
                }
            } else if (header instanceof MetaTableHeader) {
                if(abstractHeader != null) {
                    newRow.addStringCell(abstractHeader.getValue());
                } else {
                    newRow.addEmptyCell();
                }
            } else if (header instanceof MetaHeader) {
                if(abstractHeader != null) {
                    newRow.addStringCell(abstractHeader.getValue());
                } else {
                    newRow.addStringCell(header.getValue());
                }
            }
        }

        return newRow;
    }

    private ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
