package com.github.romualdrousseau.archery.intelli;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public abstract class IntelliTableStrategy {

    public List<Row> emitAllRowsForOneRow(final List<BaseHeader> headers, final BaseTableGraph graph,
            final DataTable orgTable, final BaseRow orgRow, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final RowGroup rowGroup) {
        final var newRows = new ArrayList<Row>();
        if (orgRow.isIgnored()) {
            return newRows;
        }
        this.emitAllRowsForOneRowImpl(headers, graph, orgTable, orgRow, rowGroup, pivotKeyHeader, pivotTypeHeader,
                newRows);
        return newRows;
    }

    protected abstract void emitAllRowsForOneRowImpl(final List<BaseHeader> tmpHeaders, final BaseTableGraph graph,
            final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows);

    protected String findTypeValue(final DataTable orgTable, final BaseRow orgRow,
            final DataTableHeader pivotTypeHeader) {
        if (pivotTypeHeader == null) {
            return null;
        }
        final var orgHeaders = orgTable.findAllHeaders(pivotTypeHeader);
        return orgHeaders.isEmpty() ? null : orgHeaders.get(0).getCellAtRow(orgRow).getValue();
    }

    protected void emitAllCells(final BaseTableGraph graph, final DataTable orgTable, final BaseRow orgRow,
            final RowGroup rowGroup, final BaseHeader header, final Row newRow) {
        final var orgHeaders = orgTable.findAllHeaders(header);
        if (!orgHeaders.isEmpty()) {
            emitCellsFromHeader(orgHeaders, orgTable, orgRow, rowGroup, header, newRow);
        } else {
            emitCellFromClosestHeader(graph, header, newRow);
        }
    }

    private void emitCellsFromHeader(final List<BaseHeader> orgHeaders, final DataTable orgTable, final BaseRow orgRow,
            final RowGroup rowGroup, final BaseHeader header, final Row newRow) {
        orgHeaders.forEach(orgHeader -> {
            if (rowGroup == null || !orgHeader.hasRowGroup()) {
                final var oldValue = newRow.get(header.getColumnIndex());
                final var newValue = orgHeader.getCellAtRow(orgRow).getValue();
                this.emitOneCell(header, oldValue, newValue, newRow);
            } else {
                final var newValue = rowGroup.getCell().getValue();
                this.emitOneCell(header, null, newValue, newRow);
            }
        });
    }

    private void emitCellFromClosestHeader(final BaseTableGraph graph, final BaseHeader header, final Row newRow) {
        final var orgHeader = graph.getParent().findClosestHeader(header);
        final var oldValue = newRow.get(header.getColumnIndex());
        final var newValue = orgHeader.getValue();
        this.emitOneCell(header, oldValue, newValue, newRow);
    }

    private void emitOneCell(final BaseHeader header, final String oldValue, final String newValue, final Row newRow) {
        this.updateHeaderEmptyStatus(header, newValue);
        this.updateRowValue(header, oldValue, newValue, newRow);
    }

    private void updateHeaderEmptyStatus(final BaseHeader header, final String newValue) {
        header.setColumnEmpty(header.isColumnEmpty() && StringUtils.isFastBlank(newValue));
    }

    private void updateRowValue(final BaseHeader header, final String oldValue, final String newValue,
            final Row newRow) {
        if (oldValue == null) {
            newRow.set(header.getColumnIndex(), newValue);
        } else {
            newRow.set(header.getColumnIndex(), oldValue + newValue);
        }
    }
}
