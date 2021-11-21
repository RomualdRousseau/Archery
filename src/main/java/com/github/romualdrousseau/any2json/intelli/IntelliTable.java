package com.github.romualdrousseau.any2json.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.AbstractHeader;
import com.github.romualdrousseau.any2json.base.AbstractSheet;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.intelli.header.CompositeHeader;
import com.github.romualdrousseau.any2json.intelli.header.IntelliHeader;
import com.github.romualdrousseau.any2json.intelli.header.PivotKeyHeader;
import com.github.romualdrousseau.any2json.simple.SimpleHeader;
import com.github.romualdrousseau.any2json.util.TableGraph;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class IntelliTable extends CompositeTable {

    public IntelliTable(final AbstractSheet sheet, final TableGraph root) {
        super(sheet);
        this.buildHeaders(root);
        this.buildTable(root, this.findPivotHeader());
        this.setHeaders();
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
    public BaseRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        return this.rows.get(rowIndex);
    }

    private void buildHeaders(final TableGraph graph) {
        for (final TableGraph child : graph.children()) {
            for (final Header header : child.getTable().headers()) {
                final CompositeHeader compositeHeader = (CompositeHeader) header;
                if (this.checkIfHeaderExists(compositeHeader)) {
                    continue;
                }

                CompositeHeader newHeader = compositeHeader.clone();
                newHeader.setTable(this);
                newHeader.setColumnIndex(this.tmpHeaders.size());
                this.tmpHeaders.add(newHeader);

                if (header instanceof PivotKeyHeader) {
                    newHeader = ((PivotKeyHeader) compositeHeader).getPivotValue();
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
        if (orgTable.getNumberOfRowGroups() == 0) {
            for (final Row orgRow : orgTable.rows()) {
                final ArrayList<IntelliRow> newRows = buildRowsForOneRow(orgTable, (BaseRow) orgRow, pivot, null);
                this.rows.addAll(newRows);
            }
        } else {
            for (final RowGroup rowGroup : orgTable.rowGroups()) {
                for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                    if (rowGroup.getRow() + i + 1 >= orgTable.getNumberOfRows()) {
                        break;
                    }
                    Row orgRow = orgTable.getRowAt(rowGroup.getRow() + i + 1);
                    final ArrayList<IntelliRow> newRows = buildRowsForOneRow(orgTable, (BaseRow) orgRow, pivot,
                            rowGroup);
                    this.rows.addAll(newRows);
                }
            }
        }
    }

    private ArrayList<IntelliRow> buildRowsForOneRow(final DataTable orgTable, final BaseRow orgRow,
            final PivotKeyHeader pivot, final RowGroup rowGroup) {
        final ArrayList<IntelliRow> newRows = new ArrayList<IntelliRow>();

        if (pivot == null) {
            newRows.add(buildOneRow(orgTable, orgRow, null, rowGroup));
        } else {
            for (final BaseCell pivotCell : pivot.getEntries()) {
                if (!StringUtility.isFastEmpty(orgRow.getCellAt(pivotCell.getColumnIndex()).getValue())) {
                    newRows.add(buildOneRow(orgTable, orgRow, pivotCell, rowGroup));
                }
            }
        }

        return newRows;
    }

    private IntelliRow buildOneRow(final DataTable orgTable, final BaseRow orgRow, final BaseCell pivotCell,
            final RowGroup rowGroup) {
        final IntelliRow newRow = new IntelliRow(this, this.tmpHeaders.size());

        for (final AbstractHeader abstractHeader : this.tmpHeaders) {
            List<Header> orgHeaders = orgTable.findHeader(abstractHeader);

            if (abstractHeader instanceof PivotKeyHeader && pivotCell != null) {
                if (orgHeaders.size() > 0) {
                    newRow.setCellValue(abstractHeader.getColumnIndex(), pivotCell.getValue(), pivotCell.getRawValue());
                    newRow.setCell(abstractHeader.getColumnIndex() + 1,
                            orgRow.getCellAt(pivotCell.getColumnIndex()));
                }
            } else {
                if (orgHeaders.size() > 0) {
                    for(Header orgHeader : orgHeaders) {
                        AbstractHeader orgAbstractHeader = (AbstractHeader) orgHeader;
                        if (rowGroup == null || !orgAbstractHeader.isRowGroupName()) {
                            newRow.setCell(abstractHeader.getColumnIndex(), orgAbstractHeader.getCellAtRow(orgRow));
                        } else {
                            newRow.setCell(abstractHeader.getColumnIndex(), rowGroup.getCell());
                        }
                    }
                } else {
                    newRow.setCellValue(abstractHeader.getColumnIndex(), abstractHeader.getValue(), abstractHeader.getCell().getRawValue());
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
        this.setLoadCompleted(true); // Give chance to pivot header value to update their name
        if (this.getSheet().getClassifierFactory().getTagClassifier().isPresent()) {
            for (int i = 0; i < this.tmpHeaders.size(); i++) {
                this.setHeader(i, new IntelliHeader(this.tmpHeaders.get(i)));
            }
        } else {
            for (int i = 0; i < this.tmpHeaders.size(); i++) {
                this.setHeader(i, new SimpleHeader(this.tmpHeaders.get(i)));
            }
        }
        this.setLoadCompleted(false);
    }

    private final ArrayList<CompositeHeader> tmpHeaders = new ArrayList<CompositeHeader>();
    private final ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
