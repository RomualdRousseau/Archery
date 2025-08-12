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


public class IntelliTableStrategyWithoutPivot  extends IntelliTableStrategy {

    public IntelliTableStrategyWithoutPivot() {
        // Constructor logic if needed
    }

    public void emitAllRowsForOneRowImpl(final List<BaseHeader> tmpHeaders, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows) {
        this.emitOneRow(tmpHeaders, graph, orgTable, orgRow, rowGroup).ifPresent(newRows::add);
    }

    private Optional<Row> emitOneRow(final List<BaseHeader> tmpHeaders, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup) {
        final var newRow = new Row(tmpHeaders.size());
        for (final var header : tmpHeaders) {
            this.emitAllCells(graph, orgTable, orgRow, rowGroup, header, newRow);
        }
        return Optional.of(newRow);
    }
}
