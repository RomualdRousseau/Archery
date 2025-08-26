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
import com.github.romualdrousseau.archery.header.PivotEntry;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public class IntelliTableStrategyWithPivot  extends IntelliTableStrategy {

    final private int PIVOT_KEY = 0;
    final private int PIVOT_VALUE = 1;

    public void emitAllRowsForOneRowImpl(final List<BaseHeader> headers, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows) {
        final var typeValue = findTypeValue(orgTable, orgRow, pivotTypeHeader);
        pivotKeyHeader.getEntries().stream()
                .map(entry -> emitOneRowWithPivot(headers, graph, orgTable, orgRow, rowGroup, entry, typeValue))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(newRows::add);
    }

    private Optional<Row> emitOneRowWithPivot(final List<BaseHeader> headers, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotEntry pivotEntry, final String typeValue) {
        if (!this.isValidPivotEntry(orgRow, pivotEntry, typeValue)) {
            return Optional.empty();
        }
        final var newRow = new Row(headers.size());
        final var hasPivotedValues = this.processRow(headers, graph, orgTable, orgRow, rowGroup, pivotEntry, newRow);
        return hasPivotedValues ? Optional.of(newRow) : Optional.empty();
    }

    private boolean isValidPivotEntry(final BaseRow orgRow, final PivotEntry pivotEntry, final String typeValue) {
        return orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue() &&
               (typeValue == null || typeValue.equals(pivotEntry.getTypeValue()));
    }

    private boolean processRow(final List<BaseHeader> headers, final BaseTableGraph graph,
            final DataTable orgTable, final BaseRow orgRow, final RowGroup rowGroup,
            final PivotEntry pivotEntry, final Row newRow) {
        var hasPivotedValues = false;
        for (final var header : headers) {
            if (header instanceof PivotKeyHeader) {
                hasPivotedValues |= this.emitAllCellsWithPivot(orgTable, header, pivotEntry, orgRow, newRow);
            } else {
                this.emitAllCells(graph, orgTable, orgRow, rowGroup, header, newRow);
            }
        }
        return hasPivotedValues;
    }

    private boolean emitAllCellsWithPivot(final DataTable orgTable, final BaseHeader header,
            final PivotEntry pivotEntry, final BaseRow orgRow, final Row newRow) {
        final var orgHeaders = orgTable.findAllHeaders(header);
        if (orgHeaders.isEmpty()) {
            return false;
        }
        final var columnIndex = header.getColumnIndex();
        final var entryColumnIndex = pivotEntry.getCell().getColumnIndex();
        newRow.set(columnIndex + PIVOT_KEY, pivotEntry.getCell().getValue());
        newRow.set(columnIndex + PIVOT_VALUE, orgRow.getCellAt(entryColumnIndex).getValue());
        return true;
    }
}
