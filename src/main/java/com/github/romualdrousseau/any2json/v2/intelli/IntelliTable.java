package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable extends AbstractTable {

    public IntelliTable(final TableGraph root, final ITagClassifier classifier) {
        super(classifier);
        assert(this.getClassifier() != null) : "Classifier must be defined";
        this.buildHeaders(root);
        this.buildTable(root, findPivotHeader());
        this.updateHeaderTags();
        this.setLoadCompleted(true);
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
    public AbstractRow getRowAt(final int rowIndex) {
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
                newHeader.setColumnIndex(this.getNumberOfHeaders());
                this.addHeader(newHeader);

                if (header instanceof PivotKeyHeader) {
                    newHeader = ((PivotKeyHeader) abstractHeader).getPivotValue();
                    newHeader.setTable(this);
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                }
            }
        }

        for (final TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private PivotKeyHeader findPivotHeader() {
        PivotKeyHeader result = null;
        for (final Header header : this.headers()) {
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
            final ArrayList<IntelliRow> newRows = buildRowsForOneRow(orgTable, (AbstractRow) orgRow, pivot);
            this.rows.addAll(newRows);
        }
    }

    private ArrayList<IntelliRow> buildRowsForOneRow(final DataTable orgTable, final AbstractRow orgRow,
            final PivotKeyHeader pivot) {
        final ArrayList<IntelliRow> newRows = new ArrayList<IntelliRow>();

        if (pivot == null) {
            newRows.add(buildOneRow(orgTable, orgRow, null));
        } else {
            for (final AbstractCell pivotCell : pivot.getEntries()) {
                newRows.add(buildOneRow(orgTable, orgRow, pivotCell));
            }
        }

        return newRows;
    }

    private IntelliRow buildOneRow(final DataTable orgTable, final AbstractRow orgRow, final AbstractCell pivotCell) {
        final IntelliRow newRow = new IntelliRow(this);

        for (final Header header : this.headers()) {
            final AbstractHeader abstractHeader = (AbstractHeader) header;
            final AbstractHeader orgHeader = orgTable.findHeader((AbstractHeader) header);

            if (header instanceof PivotKeyHeader && pivotCell != null) {
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

    private final ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
