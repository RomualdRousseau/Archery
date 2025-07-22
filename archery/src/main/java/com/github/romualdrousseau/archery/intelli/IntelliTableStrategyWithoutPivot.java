package com.github.romualdrousseau.archery.intelli;

import java.util.List;
import java.util.Set;

import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public class IntelliTableStrategyWithoutPivot extends IntelliTableStrategy {

    @Override
    protected void emitHeaders(final BaseTable table, final BaseHeader header, final Set<String> pivotEntryTypes) {
        this.addHeaderIntoTmpHeaders(table, header.clone(), true);
    }

    @Override
    protected void emitRows(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final RowGroup rowGroup,
            final List<Row> newRows) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var tmpHeader : this.tmpHeaders) {
            this.emitAllCells(graph, orgTable, orgRow, rowGroup, tmpHeader, newRow);
        }
        newRows.add(newRow);
    }
}
