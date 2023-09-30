package com.github.romualdrousseau.any2json.intelli;

import java.util.List;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.PivotOption;
import com.github.romualdrousseau.any2json.base.BaseHeader;
import com.github.romualdrousseau.any2json.base.BaseSheet;
import com.github.romualdrousseau.any2json.base.BaseTableGraph;
import com.github.romualdrousseau.any2json.base.DataTable;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.any2json.base.RowGroup;
import com.github.romualdrousseau.any2json.header.PivotEntry;
import com.github.romualdrousseau.any2json.header.PivotKeyHeader;

public class IntelliTable extends DataTable {

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root) {
        super(sheet);

        // Collect headers

        root.parse(e -> e.getTable().headers().forEach(h -> this.addTmpHeader((BaseHeader) h)));

        // Build tables

        final PivotKeyHeader pivot = this.findPivotHeader();
        root.parseIf(
                e -> this.buildRowsForOneTable(e, (DataTable) e.getTable(), pivot),
                e -> e.getTable() instanceof DataTable);
        this.setLoadCompleted(true);

        // Finalize headers

        this.tmpHeaders.forEach(h -> this.addHeader(new IntelliHeader(h)));
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

    private void addTmpHeader(final BaseHeader header) {
        if (this.headerAlreadyAdded(header)) {
            return;
        }

        this.pushHeader(header.clone());

        if (header instanceof PivotKeyHeader) {
            final var pivot = (PivotKeyHeader) header;
            if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
                for (final String typeValue : pivot.getEntryTypes()) {
                    this.pushHeader(pivot.getPivotType().clone().setName(typeValue));
                }
            } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
                this.pushHeader(pivot.getPivotType());
                this.pushHeader(pivot.getPivotValue());
            } else {
                this.pushHeader(pivot.getPivotValue());
            }
        }
    }

    private void buildRowsForOneTable(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivot) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            for (final var orgRow : orgTable.rows()) {
                final var newRows = buildRowsForOneRow(graph, orgTable, (BaseRow) orgRow, pivot, null);
                this.rows.addAll(newRows);
            }
        } else {
            for (final RowGroup rowGroup : orgTable.rowGroups()) {
                for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                    if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                        final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                        final var newRows = buildRowsForOneRow(graph, orgTable, orgRow, pivot, rowGroup);
                        this.rows.addAll(newRows);
                    }
                }
            }
        }
    }

    private List<IntelliRow> buildRowsForOneRow(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivot, final RowGroup rowGroup) {
        final var newRows = new ArrayList<IntelliRow>();
        if (orgRow.isIgnored()) {
            return newRows;
        }

        if (pivot == null) {
            newRows.add(buildOneRowWithoutPivot(graph, orgTable, orgRow, rowGroup));
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            for (final var value : pivot.getEntryValues()) {
                newRows.add(buildOneRowWithPivotTypeAndValue(graph, orgTable, orgRow, pivot, value, rowGroup));
            }
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            for (final var pivotEntry : pivot.getEntries()) {
                if (orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
                    newRows.add(buildOneRowWithPivotAndType(graph, orgTable, orgRow, pivotEntry, rowGroup));
                }
            }
        } else {
            for (final var pivotEntry : pivot.getEntries()) {
                if (orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
                    newRows.add(buildOneRowWithPivot(graph, orgTable, orgRow, pivotEntry, rowGroup));
                }
            }
        }
        return newRows;
    }

    private IntelliRow buildOneRowWithoutPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup) {
        final var newRow = new IntelliRow(this, this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findHeader(abstractHeader);
            this.generateCellsNoPivot(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
        }
        return newRow;
    }

    private IntelliRow buildOneRowWithPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        final var newRow = new IntelliRow(this, this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findHeader(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    newRow.setCell(abstractHeader.getColumnIndex() + 0, pivotEntry.getCell());
                    newRow.setCell(abstractHeader.getColumnIndex() + 1,
                            orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()));
                }
            } else {
                this.generateCellsNoPivot(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private IntelliRow buildOneRowWithPivotAndType(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        final var newRow = new IntelliRow(this, this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findHeader(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    final var ci = abstractHeader.getColumnIndex();
                    newRow.setCell(ci + 0, pivotEntry.getValue(), pivotEntry.getValue());
                    newRow.setCell(ci + 1, pivotEntry.getTypeValue(), pivotEntry.getTypeValue());
                    newRow.setCell(ci + 2, orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()));
                }
            } else {
                this.generateCellsNoPivot(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private IntelliRow buildOneRowWithPivotTypeAndValue(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotKeyHeader pivot, final String value, final RowGroup rowGroup) {
        final var newRow = new IntelliRow(this, this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findHeader(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    newRow.setCell(abstractHeader.getColumnIndex(), value, value);
                    int i = 1;
                    for (final var typeValue : pivot.getEntryTypes()) {
                        final var ci = abstractHeader.getColumnIndex() + i;
                        pivot.getEntries().stream()
                                .filter(x -> x.getValue().equals(value) && x.getTypeValue().equals(typeValue))
                                .findFirst()
                                .ifPresent(x -> newRow.setCell(ci, orgRow.getCellAt(x.getCell().getColumnIndex())));
                        i++;
                    }
                }
            } else {
                this.generateCellsNoPivot(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private void generateCellsNoPivot(final BaseTableGraph graph, final List<Header> orgHeaders,
            final BaseHeader abstractHeader, final RowGroup rowGroup, final BaseRow orgRow, final IntelliRow newRow) {
        if (orgHeaders.size() > 0) {
            for (final var orgHeader : orgHeaders) {
                final var orgAbstractHeader = (BaseHeader) orgHeader;
                if (rowGroup == null || !orgAbstractHeader.hasRowGroup()) {
                    newRow.setCell(abstractHeader.getColumnIndex(), orgAbstractHeader.getCellAtRow(orgRow));
                } else {
                    newRow.setCell(abstractHeader.getColumnIndex(), rowGroup.getCell());
                }
            }
        } else {
            final var header = graph.getParent().findClosestHeader(abstractHeader);
            newRow.setCell(abstractHeader.getColumnIndex(), header.getValue(), header.getCell().getRawValue());
        }
    }

    private PivotKeyHeader findPivotHeader() {
        PivotKeyHeader result = null;
        for (final var header : this.tmpHeaders) {
            if (header instanceof PivotKeyHeader) {
                result = (PivotKeyHeader) header;
                break;
            }
        }
        return result;
    }

    private boolean headerAlreadyAdded(final BaseHeader header) {
        return this.tmpHeaders.contains(header);
    }

    private void pushHeader(final BaseHeader newHeader) {
        newHeader.setTable(this);
        newHeader.setColumnIndex(this.tmpHeaders.size());
        this.tmpHeaders.add(newHeader);
    }

    private final ArrayList<BaseHeader> tmpHeaders = new ArrayList<>();
    private final ArrayList<IntelliRow> rows = new ArrayList<>();
}
