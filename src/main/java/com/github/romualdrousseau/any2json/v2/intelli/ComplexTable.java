package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.header.IntelliHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class ComplexTable extends IntelliTable {

    public ComplexTable(final TableGraph root, final ITagClassifier classifier) {
        super(classifier);
        this.buildHeaders(root);
        this.buildTable(root, findPivotHeader());
        this.setLoadCompleted(true);
        this.setHeaders();
        this.updateHeaderTags();
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
    public BaseRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        return this.rows.get(rowIndex);
    }

    private void buildHeaders(final TableGraph graph) {
        for (final TableGraph child : graph.children()) {
            for (final Header header : child.getTable().headers()) {
                final AbstractHeader abstractHeader = (AbstractHeader) header;
                if (this.checkIfHeaderExists(abstractHeader)) {
                    continue;
                }

                AbstractHeader newHeader = abstractHeader.clone();
                newHeader.setTable(this);
                newHeader.setColumnIndex(this.tmpHeaders.size());
                this.tmpHeaders.add(newHeader);

                if (header instanceof PivotKeyHeader) {
                    newHeader = ((PivotKeyHeader) abstractHeader).getPivotValue();
                    newHeader.setTable(this);
                    newHeader.setColumnIndex(this.tmpHeaders.size());
                    this.tmpHeaders.add(newHeader);
                }
            }
        }

        for (final TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private PivotKeyHeader findPivotHeader() {
        PivotKeyHeader result = null;
        for (final Header header : this.tmpHeaders) {
            if (header instanceof PivotKeyHeader) {
                result = (PivotKeyHeader) header;
                break;
            }
        }
        return result;
    }

    private void buildTable(final TableGraph graph, final PivotKeyHeader pivot) {
        for (final TableGraph child : graph.children()) {
            if (child.getTable() instanceof DataTable) {
                this.buildRowsForOneTable((DataTable) child.getTable(), pivot);
            }
        }

        for (final TableGraph child : graph.children()) {
            buildTable(child, pivot);
        }
    }

    private void buildRowsForOneTable(final DataTable orgTable, final PivotKeyHeader pivot) {
        for (final Row orgRow : orgTable.rows()) {
            final ArrayList<IntelliRow> newRows = buildRowsForOneRow(orgTable, (BaseRow) orgRow, pivot);
            this.rows.addAll(newRows);
        }
    }

    private ArrayList<IntelliRow> buildRowsForOneRow(final DataTable orgTable, final BaseRow orgRow,
            final PivotKeyHeader pivot) {
        final ArrayList<IntelliRow> newRows = new ArrayList<IntelliRow>();

        if (pivot == null) {
            newRows.add(buildOneRow(orgTable, orgRow, null));
        } else {
            for (final BaseCell pivotCell : pivot.getEntries()) {
                newRows.add(buildOneRow(orgTable, orgRow, pivotCell));
            }
        }

        return newRows;
    }

    private IntelliRow buildOneRow(final DataTable orgTable, final BaseRow orgRow, final BaseCell pivotCell) {
        final IntelliRow newRow = new IntelliRow(this, this.tmpHeaders.size());

        for (final AbstractHeader abstractHeader : this.tmpHeaders) {
            final AbstractHeader orgHeader = orgTable.findHeader(abstractHeader);

            if (abstractHeader instanceof PivotKeyHeader && pivotCell != null) {
                if (orgHeader != null) {
                    newRow.setCellValue(abstractHeader.getColumnIndex(), pivotCell.getValue());
                    newRow.setCell(abstractHeader.getColumnIndex() + 1, orgRow.getCellAt(pivotCell.getColumnIndex()));
                }
            } else {
                if (orgHeader != null) {
                    newRow.setCell(abstractHeader.getColumnIndex(), orgHeader.getCellForRow(orgRow));
                } else {
                    newRow.setCellValue(abstractHeader.getColumnIndex(), abstractHeader.getValue());
                }
            }
        }

        return newRow;
    }

    private boolean checkIfHeaderExists(final AbstractHeader header) {
        return this.tmpHeaders.contains(header);
    }

    private void setHeaders() {
        for (int i = 0; i < this.tmpHeaders.size(); i++) {
            this.addHeader(this.tmpHeaders.get(i));
        }
        for (int i = 0; i < this.tmpHeaders.size(); i++) {
            this.setHeader(i, new IntelliHeader(this.tmpHeaders.get(i)));
        }
    }

    private final ArrayList<AbstractHeader> tmpHeaders = new ArrayList<AbstractHeader>();
    private final ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
