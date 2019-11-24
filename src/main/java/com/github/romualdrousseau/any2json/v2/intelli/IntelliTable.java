package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractCell;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotTableHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable extends AbstractTable {

    public IntelliTable(final TableGraph root, final ITagClassifier classifier) {
        super(classifier);
        this.buildHeaders(root);

        PivotTableHeader pivot = null;
        for (final Header header : this.headers()) {
            if (header instanceof PivotTableHeader) {
                pivot = (PivotTableHeader) header;
                break;
            }
        }

        this.buildTable(root, pivot);
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
                newHeader.setColumnIndex(this.getNumberOfHeaders());
                this.addHeader(newHeader);

                if (header instanceof PivotTableHeader) {
                    newHeader = ((PivotTableHeader) abstractHeader).cloneAsValueHeader();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                }
            }
        }

        for (final TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private void buildTable(final TableGraph graph, final PivotTableHeader pivot) {
        for (final TableGraph child : graph.children()) {
            if (child.getTable() instanceof DataTable) {
                this.buildRowsForOneTable((DataTable) child.getTable(), pivot);
            }
        }

        for (final TableGraph child : graph.children()) {
            buildTable(child, pivot);
        }
    }

    private void buildRowsForOneTable(final DataTable orgTable, final PivotTableHeader pivot) {
        for (final Row orgRow : orgTable.rows()) {
            final ArrayList<IntelliRow> newRows = buildRowsForOneRow(orgTable, (AbstractRow) orgRow, pivot);
            this.rows.addAll(newRows);
        }
    }

    private ArrayList<IntelliRow> buildRowsForOneRow(final DataTable orgTable, final AbstractRow orgRow,
            final PivotTableHeader pivot) {
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

            if (header instanceof PivotTableHeader && pivotCell != null) {
                if (orgHeader != null) {
                    if (((PivotTableHeader) header).isPivotalValue()) {
                        newRow.setCell(abstractHeader.getColumnIndex(), orgRow.getCellAt(pivotCell.getColumnIndex()));
                    } else {
                        newRow.setCellValue(abstractHeader.getColumnIndex(), pivotCell.getValue());
                    }
                }
            } else {
                if (orgHeader != null) {
                    newRow.setCell(abstractHeader.getColumnIndex(), orgHeader.getCell(orgRow));
                } else {
                    newRow.setCellValue(abstractHeader.getColumnIndex(), abstractHeader.getValue());
                }
            }
        }

        return newRow;
    }

    private final ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
