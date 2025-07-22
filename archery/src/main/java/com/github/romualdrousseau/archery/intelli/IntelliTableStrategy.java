package com.github.romualdrousseau.archery.intelli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public abstract class IntelliTableStrategy {

    protected final ArrayList<BaseHeader> tmpHeaders = new ArrayList<>();

    public List<BaseHeader> getTmpHeaders() {
        return this.tmpHeaders;
    }

    public void addTmpHeaders(final BaseTable table, final Set<String> pivotEntryTypes) {
        table.headers().forEach(h -> this.addTmpHeader(table, (BaseHeader) h, pivotEntryTypes));
    }

    public PivotKeyHeader findKeyPivotHeader() {
        return this.findTmpHeader(PivotKeyHeader.class, header -> header instanceof PivotKeyHeader);
    }

    public DataTableHeader findPivotTypeHeader() {
        return this.findTmpHeader(DataTableHeader.class, BaseHeader::isPivotTypeHeader);
    }

    public void emitAllRowsForOneTable(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final DataFrameWriter writer) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            emitRowsForTableWithoutRowGroups(graph, orgTable, pivotKeyHeader, pivotTypeHeader, writer);
        } else {
            emitRowsForTableWithRowGroups(graph, orgTable, pivotKeyHeader, pivotTypeHeader, writer);
        }
    }

    protected abstract void emitHeaders(final BaseTable table, final BaseHeader header,
            final Set<String> pivotEntryTypes);

    protected abstract void emitRows(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final RowGroup rowGroup,
            final List<Row> newRows);

    protected void addHeaderIntoTmpHeaders(final BaseTable table, final BaseHeader newHeader,
            final boolean columnEmpty) {
        newHeader.setTable(table);
        newHeader.setColumnIndex(this.tmpHeaders.size());
        newHeader.setColumnEmpty(columnEmpty);
        this.tmpHeaders.add(newHeader);
    }

    protected String findTypeValue(final DataTable orgTable, final BaseRow orgRow,
            final DataTableHeader pivotTypeHeader) {
        if (pivotTypeHeader != null) {
            final var orgHeaders = orgTable.findAllHeaders(pivotTypeHeader);
            if (orgHeaders.size() > 0) {
                return orgHeaders.get(0).getCellAtRow(orgRow).getValue();
            }
        }
        return null;
    }

    protected void emitAllCells(final BaseTableGraph graph, final DataTable orgTable, final BaseRow orgRow,
            final RowGroup rowGroup, final BaseHeader tmpHeader, final Row newRow) {
        final var orgHeaders = orgTable.findAllHeaders(tmpHeader);
        if (orgHeaders.size() > 0) {
            emitCellsForHeaders(orgTable, orgRow, rowGroup, tmpHeader, newRow, orgHeaders);
        } else {
            emitCellForClosestHeader(graph, tmpHeader, newRow);
        }
    }

    private void emitCellsForHeaders(final DataTable orgTable, final BaseRow orgRow, final RowGroup rowGroup,
            final BaseHeader tmpHeader, final Row newRow, final List<BaseHeader> orgHeaders) {
        for (final var orgHeader : orgHeaders) {
            if (rowGroup == null || !orgHeader.hasRowGroup()) {
                emitCellForHeader(orgRow, tmpHeader, newRow, orgHeader);
            } else {
                emitCellForRowGroup(rowGroup, tmpHeader, newRow);
            }
        }
    }

    private void emitCellForHeader(final BaseRow orgRow, final BaseHeader tmpHeader, final Row newRow,
            final BaseHeader orgHeader) {
        final var oldValue = newRow.get(tmpHeader.getColumnIndex());
        final var newValue = orgHeader.getCellAtRow(orgRow).getValue();
        this.emitOneCell(tmpHeader, oldValue, newValue, newRow);
    }

    private void emitCellForRowGroup(final RowGroup rowGroup, final BaseHeader tmpHeader, final Row newRow) {
        final var newValue = rowGroup.getCell().getValue();
        this.emitOneCell(tmpHeader, null, newValue, newRow);
    }

    private void emitCellForClosestHeader(final BaseTableGraph graph, final BaseHeader tmpHeader, final Row newRow) {
        final var orgHeader = graph.getParent().findClosestHeader(tmpHeader);
        final var oldValue = newRow.get(tmpHeader.getColumnIndex());
        final var newValue = orgHeader.getValue();
        this.emitOneCell(tmpHeader, oldValue, newValue, newRow);
    }

    private void emitOneCell(final BaseHeader tmpHeader, final String oldValue, final String newValue,
            final Row newRow) {
        tmpHeader.setColumnEmpty(tmpHeader.isColumnEmpty() && StringUtils.isFastBlank(newValue));
        if (oldValue == null) {
            newRow.set(tmpHeader.getColumnIndex(), newValue);
        } else {
            newRow.set(tmpHeader.getColumnIndex(), oldValue + newValue);
        }
    }

    private <T extends BaseHeader> T findTmpHeader(final Class<T> headerClass, final Predicate<BaseHeader> predicate) {
        for (final var header : this.tmpHeaders) {
            if (predicate.test(header)) {
                return headerClass.cast(header);
            }
        }
        return null;
    }

    private void addTmpHeader(final BaseTable table, final BaseHeader header, final Set<String> pivotEntryTypes) {
        if (this.tmpHeaders.contains(header)) {
            return;
        }
        this.emitHeaders(table, header, pivotEntryTypes);
    }

    private void emitRowsForTableWithoutRowGroups(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final DataFrameWriter writer) {
        for (final var orgRow : orgTable.rows()) {
            final var newRows = emitAllRowsForOneRow(graph, orgTable, (BaseRow) orgRow, pivotKeyHeader, pivotTypeHeader,
                    null);
            writeRows(writer, newRows);
        }
    }

    private void emitRowsForTableWithRowGroups(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final DataFrameWriter writer) {
        for (final RowGroup rowGroup : orgTable.rowGroups()) {
            for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                    final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                    final var newRows = emitAllRowsForOneRow(graph, orgTable, orgRow, pivotKeyHeader, pivotTypeHeader,
                            rowGroup);
                    writeRows(writer, newRows);
                }
            }
        }
    }

    private List<Row> emitAllRowsForOneRow(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final RowGroup rowGroup) {
        final var newRows = new ArrayList<Row>();
        if (orgRow.isIgnored()) {
            return newRows;
        }
        this.emitRows(graph, orgTable, orgRow, pivotKeyHeader, pivotTypeHeader, rowGroup, newRows);
        return newRows;
    }

    private void writeRows(final DataFrameWriter writer, final List<Row> newRows) {
        newRows.forEach(row -> {
            try {
                writer.write(row);
            } catch (final IOException x) {
                throw new UncheckedIOException(x);
            }
        });
    }
}
