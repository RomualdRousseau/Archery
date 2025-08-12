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

    public List<Row> emitAllRowsForOneRow(final List<BaseHeader> headers, final BaseTableGraph graph, final DataTable orgTable, final BaseRow orgRow, final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final RowGroup rowGroup) {
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
        if (pivotTypeHeader != null) {
            final var orgHeaders = orgTable.findAllHeaders(pivotTypeHeader);
            if (orgHeaders.size() > 0) {
                return orgHeaders.get(0).getCellAtRow(orgRow).getValue();
            }
        }
        return null;
    }

    protected void emitAllCells(final BaseTableGraph graph, final DataTable orgTable, final BaseRow orgRow,
            final RowGroup rowGroup,
            final BaseHeader tmpHeader, final Row newRow) {
        final var orgHeaders = orgTable.findAllHeaders(tmpHeader);
        if (orgHeaders.size() > 0) {
            for (final var orgHeader : orgHeaders) {
                if (rowGroup == null || !orgHeader.hasRowGroup()) {
                    final var oldValue = newRow.get(tmpHeader.getColumnIndex());
                    final var newValue = orgHeader.getCellAtRow(orgRow).getValue();
                    this.emitOneCell(tmpHeader, oldValue, newValue, newRow);
                } else {
                    final var newValue = rowGroup.getCell().getValue();
                    this.emitOneCell(tmpHeader, null, newValue, newRow);
                }
            }
        } else {
            final var orgHeader = graph.getParent().findClosestHeader(tmpHeader);
            final var oldValue = newRow.get(tmpHeader.getColumnIndex());
            final var newValue = orgHeader.getValue();
            this.emitOneCell(tmpHeader, oldValue, newValue, newRow);
        }
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
}
