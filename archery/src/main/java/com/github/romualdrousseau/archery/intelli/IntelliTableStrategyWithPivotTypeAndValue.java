package com.github.romualdrousseau.archery.intelli;

import java.util.List;
import java.util.Optional;

import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public class IntelliTableStrategyWithPivotTypeAndValue extends IntelliTableStrategy {

    public void emitAllRowsForOneRowImpl(final List<BaseHeader> headers, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows) {
        final var typeValue = this.findTypeValue(orgTable, orgRow, pivotTypeHeader);
        pivotKeyHeader.getEntryPivotValues().stream()
                .map(pivotValue -> emitOneRowWithPivotTypeAndValue(headers, graph, orgTable, orgRow, rowGroup, pivotKeyHeader, pivotValue, typeValue))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(newRows::add);
    }

    private Optional<Row> emitOneRowWithPivotTypeAndValue(final List<BaseHeader> headers, final BaseTableGraph graph,
            final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader, final String pivotValue,
            final String typeValue) {
        final var newRow = new Row(headers.size());
        final var hasPivotedValues = processRow(headers, graph, orgTable, orgRow, rowGroup, pivotKeyHeader,
                pivotValue, typeValue, newRow);
        return hasPivotedValues ? Optional.of(newRow) : Optional.empty();
    }

    private boolean processRow(final List<BaseHeader> headers, final BaseTableGraph graph,
            final DataTable orgTable, final BaseRow orgRow, final RowGroup rowGroup,
            final PivotKeyHeader pivotKeyHeader, final String pivotValue, final String typeValue,
            final Row newRow) {
        var hasPivotedValues = false;
        for (final var header : headers) {
            if (header instanceof PivotKeyHeader) {
                hasPivotedValues |= this.emitAllCellsWithPivotTypeAndValue(orgTable, orgRow, (PivotKeyHeader) header, pivotKeyHeader,
                        pivotValue, typeValue, newRow);
            } else {
                this.emitAllCells(graph, orgTable, orgRow, rowGroup, header, newRow);
            }
        }
        return hasPivotedValues;
    }

    private boolean emitAllCellsWithPivotTypeAndValue(final DataTable orgTable, final BaseRow orgRow,
            final PivotKeyHeader header, final PivotKeyHeader pivotKeyHeader, final String pivotValue,
            final String typeValue, final Row newRow) {
        final var orgHeaders = orgTable.findAllHeaders(header);
        if (orgHeaders.isEmpty()) {
            return false;
        }

        final var columnIndex = header.getColumnIndex();
        newRow.set(columnIndex, pivotValue);

        var hasPivotedValues = false;
        int currentIndex = 1;
        for (final var entryTypeValue : pivotKeyHeader.getEntryTypeValues()) {
            hasPivotedValues |= this.emitAllCellsForEntries(orgRow, pivotKeyHeader, pivotValue, typeValue, entryTypeValue, columnIndex + currentIndex++, newRow);
        }
        return hasPivotedValues;
    }

    private boolean emitAllCellsForEntries(final BaseRow orgRow, final PivotKeyHeader pivotKeyHeader,
            final String pivotValue, final String typeValue, final String entryTypeValue,
            final int columnIndex, final Row newRow) {
        return pivotKeyHeader.getEntries().stream()
                .filter(entry -> entry.getPivotValue().equals(pivotValue) &&
                        ((typeValue == null && entry.getTypeValue().equals(entryTypeValue)) ||
                                (typeValue != null && typeValue.equals(entryTypeValue))))
                .findFirst()
                .map(entry -> {
                    final var cellValue = orgRow.getCellAt(entry.getCell().getColumnIndex()).getValue();
                    newRow.set(columnIndex, cellValue);
                    return true;
                })
                .orElse(false);
    }
}
