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

    public IntelliTableStrategyWithPivotTypeAndValue() {
        // Constructor logic if needed
    }

    public void emitAllRowsForOneRowImpl(final List<BaseHeader> headers, final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader, final List<Row> newRows) {
        final var typeValue = this.findTypeValue(orgTable, orgRow, pivotTypeHeader);
        pivotKeyHeader.getEntryPivotValues()
                .forEach(x -> this
                        .emitOneRowWithPivotTypeAndValue(headers, graph, orgTable, orgRow, rowGroup, pivotKeyHeader, x,
                                typeValue)
                        .ifPresent(newRows::add));
    }

    private Optional<Row> emitOneRowWithPivotTypeAndValue(final List<BaseHeader> headers, final BaseTableGraph graph,
            final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader, final String pivotValue,
            final String typeValue) {
        boolean hasPivotedValues = false;
        final var newRow = new Row(headers.size());
        for (final var tmpHeader : headers) {
            if (tmpHeader instanceof PivotKeyHeader) {
                final var orgHeaders = orgTable.findAllHeaders(tmpHeader);
                if (orgHeaders.size() > 0) {
                    final var ci = tmpHeader.getColumnIndex();
                    newRow.set(ci, pivotValue);
                    int i = 1;
                    for (final var entryTypeValue : pivotKeyHeader.getEntryTypeValues()) {
                        final var tv_i = entryTypeValue;
                        final var pv_i = pivotValue;
                        final var ci_i = ci + i++;
                        hasPivotedValues |= pivotKeyHeader.getEntries().stream()
                                .filter(x -> x.getPivotValue().equals(pv_i) &&
                                        (typeValue == null && x.getTypeValue().equals(tv_i)
                                                || typeValue != null && typeValue.equals(tv_i)))
                                .findFirst()
                                .map(x -> {
                                    final var cell = orgRow.getCellAt(x.getCell().getColumnIndex());
                                    newRow.set(ci_i, cell.getValue());
                                    return cell.hasValue();
                                })
                                .orElse(false);
                    }
                }
            } else {
                this.emitAllCells(graph, orgTable, orgRow, rowGroup, tmpHeader, newRow);
            }
        }
        return hasPivotedValues ? Optional.of(newRow) : Optional.empty();
    }
}
