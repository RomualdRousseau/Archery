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

    public IntelliTableStrategyWithPivot() {
    }

    public void emitAllRowsForOneRowImpl(final List<BaseHeader> tmpHeaders, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows) {
        final var typeValue = this.findTypeValue(orgTable, orgRow, pivotTypeHeader);
        pivotKeyHeader.getEntries()
                        .forEach(x -> this.emitOneRowWithPivot(tmpHeaders, graph, orgTable, orgRow, rowGroup, x, typeValue)
                                .ifPresent(newRows::add));
    }

    private Optional<Row> emitOneRowWithPivot(final List<BaseHeader>tmpHeaders, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotEntry pivotEntry, final String typeValue) {
        if (!orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
            return Optional.empty();
        }
        if (typeValue != null && !typeValue.equals(pivotEntry.getTypeValue())) {
            return Optional.empty();
        }
        final var newRow = new Row(tmpHeaders.size());
        for (final var tmpHeader : tmpHeaders) {
            if (tmpHeader instanceof PivotKeyHeader) {
                final var orgHeaders = orgTable.findAllHeaders(tmpHeader);
                if (orgHeaders.size() > 0) {
                    final var ci = tmpHeader.getColumnIndex();
                    newRow.set(ci + 0, pivotEntry.getCell().getValue());
                    newRow.set(ci + 1, orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).getValue());
                }
            } else {
                this.emitAllCells(graph, orgTable, orgRow, rowGroup, tmpHeader, newRow);
            }
        }
        return Optional.of(newRow);
    }
}
