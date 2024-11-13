package com.github.romualdrousseau.archery.intelli;

import java.util.List;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;

import com.github.romualdrousseau.archery.PivotOption;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.header.PivotEntry;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;
import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class IntelliTable extends DataTable {

    private static final int BATCH_SIZE = 10000;

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root, final boolean headerAutoNameEnabled) {
        super(sheet);

        // Collect headers

        root.parse(e -> e.getTable().headers().forEach(h -> this.addTmpHeader((BaseHeader) h)));

        // Build tables

        try {
            this.writer = new DataFrameWriter(BATCH_SIZE, this.tmpHeaders.size());
            final var pivot = this.findPivotHeader();
            root.parseIf(
                    e -> this.buildRowsForOneTable((BaseTableGraph) e, (DataTable) e.getTable(), pivot),
                    e -> e instanceof BaseTableGraph && e.getTable() instanceof DataTable);
            this.setLoadCompleted(true);
            this.rows = this.writer.getDataFrame();
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }

        // Finalize headers

        this.tmpHeaders.forEach(h -> this.addHeader(new IntelliHeader(h, !headerAutoNameEnabled)));
    }

    @Override
    public void close() throws IOException {
        this.rows.close();
        this.writer.close();
    }

    @Override
    public int getNumberOfColumns() {
        return this.getNumberOfHeaders();
    }

    @Override
    public int getNumberOfRows() {
        return this.rows.getRowCount();
    }

    @Override
    public BaseRow getRowAt(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }
        return new IntelliRow(this, rowIndex, this.rows.getRow(rowIndex));
    }

    private void addTmpHeader(final BaseHeader header) {
        if (this.tmpHeaders.contains(header)) {
            return;
        }

        if (header instanceof PivotKeyHeader) {
            final var pivot = (PivotKeyHeader) header;
            if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
                this.addHeaderIntoTmpHeaders(pivot.clone(), false);
                for (final String typeValue : pivot.getEntryTypes()) {
                    this.addHeaderIntoTmpHeaders(pivot.getPivotType().clone().setName(typeValue), false);
                }
            } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
                this.addHeaderIntoTmpHeaders(pivot.clone(), false);
                this.addHeaderIntoTmpHeaders(pivot.getPivotType().clone(), false);
                this.addHeaderIntoTmpHeaders(pivot.getPivotValue().clone(), false);
            } else {
                this.addHeaderIntoTmpHeaders(pivot.clone(), false);
                this.addHeaderIntoTmpHeaders(pivot.getPivotValue().clone(), false);
            }
        } else {
            this.addHeaderIntoTmpHeaders(header.clone(), true);
        }
    }

    private void buildRowsForOneTable(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivot) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            for (final var orgRow : orgTable.rows()) {
                final var newRows = buildRowsForOneRow(graph, orgTable, (BaseRow) orgRow, pivot, null);
                this.addRows(newRows);
            }
        } else {
            for (final RowGroup rowGroup : orgTable.rowGroups()) {
                for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                    if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                        final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                        final var newRows = buildRowsForOneRow(graph, orgTable, orgRow, pivot, rowGroup);
                        this.addRows(newRows);
                    }
                }
            }
        }
    }

    private List<Row> buildRowsForOneRow(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivot, final RowGroup rowGroup) {
        final var newRows = new ArrayList<Row>();
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

    private Row buildOneRowWithoutPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
        }
        return newRow;
    }

    private Row buildOneRowWithPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    newRow.set(abstractHeader.getColumnIndex() + 0, pivotEntry.getCell().getValue());
                    newRow.set(abstractHeader.getColumnIndex() + 1,
                            orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).getValue());
                }
            } else {
                this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private Row buildOneRowWithPivotAndType(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    final var ci = abstractHeader.getColumnIndex();
                    newRow.set(ci + 0, pivotEntry.getValue());
                    newRow.set(ci + 1, pivotEntry.getTypeValue());
                    newRow.set(ci + 2, orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).getValue());
                }
            } else {
                this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private Row buildOneRowWithPivotTypeAndValue(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotKeyHeader pivot, final String value, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    newRow.set(abstractHeader.getColumnIndex(), value);
                    int i = 1;
                    for (final var typeValue : pivot.getEntryTypes()) {
                        final var ci = abstractHeader.getColumnIndex() + i;
                        pivot.getEntries().stream()
                                .filter(x -> x.getValue().equals(value) && x.getTypeValue().equals(typeValue))
                                .findFirst()
                                .ifPresent(
                                        x -> newRow.set(ci, orgRow.getCellAt(x.getCell().getColumnIndex()).getValue()));
                        i++;
                    }
                }
            } else {
                this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return newRow;
    }

    private void generateCells(final BaseTableGraph graph, final List<BaseHeader> orgHeaders,
            final BaseHeader abstractHeader, final RowGroup rowGroup, final BaseRow orgRow, final Row newRow) {
        if (orgHeaders.size() > 0) {
            for (final var orgHeader : orgHeaders) {
                final var orgAbstractHeader = (BaseHeader) orgHeader;
                if (rowGroup == null || !orgAbstractHeader.hasRowGroup()) {
                    final var currValue = orgAbstractHeader.getCellAtRow(orgRow).getValue();
                    final var prevValue = newRow.get(abstractHeader.getColumnIndex());
                    this.generateCell(abstractHeader, prevValue, currValue, newRow);
                } else {
                    final var currValue = rowGroup.getCell().getValue();
                    this.generateCell(abstractHeader, null, currValue, newRow);
                }

            }
        } else {
            final var orgAbstractHeader = graph.getParent().findClosestHeader(abstractHeader);
            final var currValue = orgAbstractHeader.getValue();
            final var prevValue = newRow.get(abstractHeader.getColumnIndex());
            this.generateCell(abstractHeader, prevValue, currValue, newRow);
        }
    }

    private void generateCell(final BaseHeader abstractHeader, final String prevValue, final String currValue, final Row newRow) {
        abstractHeader.setColumnEmpty(abstractHeader.isColumnEmpty() && StringUtils.isFastBlank(currValue));
        if (prevValue == null) {
            newRow.set(abstractHeader.getColumnIndex(), currValue);
        } else {
            newRow.set(abstractHeader.getColumnIndex(), prevValue + currValue);
        }
    }

    private PivotKeyHeader findPivotHeader() {
        for (final var header : this.tmpHeaders) {
            if (header instanceof PivotKeyHeader) {
                return (PivotKeyHeader) header;
            }
        }
        return null;
    }

    private void addHeaderIntoTmpHeaders(final BaseHeader newHeader, final boolean columnEmpty) {
        newHeader.setTable(this);
        newHeader.setColumnIndex(this.tmpHeaders.size());
        newHeader.setColumnEmpty(columnEmpty);
        this.tmpHeaders.add(newHeader);
    }

    private void addRows(final List<Row> newRows) {
        newRows.forEach(row -> {
            try {
                this.writer.write(row);
            } catch (final IOException x) {
                throw new UncheckedIOException(x);
            }
        });
    }

    private final ArrayList<BaseHeader> tmpHeaders = new ArrayList<>();
    private final DataFrameWriter writer;
    private final DataFrame rows;
}
