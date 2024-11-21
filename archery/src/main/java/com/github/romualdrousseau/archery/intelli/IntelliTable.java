package com.github.romualdrousseau.archery.intelli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.archery.PivotOption;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotEntry;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public class IntelliTable extends DataTable {

    private static final int BATCH_SIZE = 10000;

    private final ArrayList<BaseHeader> tmpHeaders = new ArrayList<>();
    private final DataFrameWriter writer;
    private final DataFrame rows;

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root, final boolean headerAutoNameEnabled) {
        super(sheet);

        // Collect pivot types

        final var pivotEntryTypes = new HashSet<String>();
        root.parse(e -> e.getTable().headers().forEach(h -> this.addPivotEntryTypes((BaseHeader) h, pivotEntryTypes)));

        // Collect headers

        root.parse(e -> e.getTable().headers().forEach(h -> this.addTmpHeader((BaseHeader) h, pivotEntryTypes)));

        final var pivotKeyHeader = this.findKeyPivotHeader();
        final var pivotTypeHeader = this.findPivotTypeHeader();

        // Build tables

        try {
            this.writer = new DataFrameWriter(BATCH_SIZE, this.tmpHeaders.size());
            root.parseIf(
                    e -> this.emitAllRowsForOneTable((BaseTableGraph) e, (DataTable) e.getTable(), pivotKeyHeader,
                            pivotTypeHeader),
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

    private void addPivotEntryTypes(final BaseHeader header, final Set<String> pivotEntryTypes) {
        if (!header.isPivotTypeHeader()) {
            return;
        }

        pivotEntryTypes.addAll(StreamSupport.stream(header.getTable().rows().spliterator(), false)
                .filter(x -> header.getCellAtRow(x).hasValue())
                .map(x -> header.getCellAtRow(x).getValue())
                .distinct()
                .toList());
    }

    private void addTmpHeader(final BaseHeader header, final Set<String> pivotEntryTypes) {
        if (this.tmpHeaders.contains(header)) {
            return;
        }

        if (header instanceof PivotKeyHeader) {
            final var pivotHeader = (PivotKeyHeader) header.clone();
            if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
                if (!pivotEntryTypes.isEmpty()) {
                    pivotHeader.setEntryTypeValues(pivotEntryTypes);
                }
                this.addHeaderIntoTmpHeaders(pivotHeader, false);
                pivotHeader.getEntryTypeValues().forEach(
                        x -> this.addHeaderIntoTmpHeaders(pivotHeader.getPivotTypeHeader().clone().setName(x), false));
            } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
                this.addHeaderIntoTmpHeaders(pivotHeader, false);
                this.addHeaderIntoTmpHeaders(pivotHeader.getPivotTypeHeader().clone(), false);
                this.addHeaderIntoTmpHeaders(pivotHeader.getPivotValueHeader().clone(), false);
            } else {
                this.addHeaderIntoTmpHeaders(pivotHeader, false);
                this.addHeaderIntoTmpHeaders(pivotHeader.getPivotValueHeader().clone(), false);
            }
        } else {
            this.addHeaderIntoTmpHeaders(header.clone(), true);
        }
    }

    private PivotKeyHeader findKeyPivotHeader() {
        for (final var header : this.tmpHeaders) {
            if (header instanceof PivotKeyHeader) {
                return (PivotKeyHeader) header;
            }
        }
        return null;
    }

    private DataTableHeader findPivotTypeHeader() {
        for (final var header : this.tmpHeaders) {
            if (header.isPivotTypeHeader()) {
                return (DataTableHeader) header;
            }
        }
        return null;
    }

    private String findTypeValue(final DataTable orgTable, final BaseRow orgRow,
            final DataTableHeader pivotTypeHeader) {
        if (pivotTypeHeader != null) {
            final var orgHeaders = orgTable.findAllHeaders(pivotTypeHeader);
            if (orgHeaders.size() > 0) {
                return orgHeaders.get(0).getCellAtRow(orgRow).getValue();
            }
        }
        return null;
    }

    private void emitAllRowsForOneTable(final BaseTableGraph graph, final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            for (final var orgRow : orgTable.rows()) {
                final var newRows = emitAllRowsForOneRow(graph, orgTable, (BaseRow) orgRow, pivotKeyHeader,
                        pivotTypeHeader,
                        null);
                this.addRows(newRows);
            }
        } else {
            for (final RowGroup rowGroup : orgTable.rowGroups()) {
                for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                    if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                        final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                        final var newRows = emitAllRowsForOneRow(graph, orgTable, orgRow, pivotKeyHeader,
                                pivotTypeHeader,
                                rowGroup);
                        this.addRows(newRows);
                    }
                }
            }
        }
    }

    private List<Row> emitAllRowsForOneRow(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader, final RowGroup rowGroup) {
        final var newRows = new ArrayList<Row>();
        if (orgRow.isIgnored()) {
            return newRows;
        }
        if (pivotKeyHeader == null) {
            this.emitOneRowWithoutPivot(graph, orgTable, orgRow, rowGroup).ifPresent(newRows::add);
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            pivotKeyHeader.getEntryPivotValues()
                    .forEach(x -> this
                            .emitOneRowWithPivotTypeAndValue(graph, orgTable, orgRow, rowGroup, pivotKeyHeader, x,
                                    this.findTypeValue(orgTable, orgRow, pivotTypeHeader))
                            .ifPresent(newRows::add));
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            pivotKeyHeader.getEntries()
                    .forEach(x -> this.emitOneRowWithPivotAndType(graph, orgTable, orgRow, rowGroup, x)
                            .ifPresent(newRows::add));
        } else {
            pivotKeyHeader.getEntries()
                    .forEach(x -> this.emitOneRowWithPivot(graph, orgTable, orgRow, rowGroup, x)
                            .ifPresent(newRows::add));
        }
        return newRows;
    }

    private Optional<Row> emitOneRowWithoutPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var tmpHeader : this.tmpHeaders) {
            this.emitAllCells(graph, orgTable, orgRow, rowGroup, tmpHeader, newRow);
        }
        return Optional.of(newRow);
    }

    private Optional<Row> emitOneRowWithPivotTypeAndValue(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotKeyHeader pivotKeyHeader, final String pivotValue,
            final String typeValue) {
        boolean hasPivotedValues = false;

        final var newRow = new Row(this.tmpHeaders.size());
        for (final var tmpHeader : this.tmpHeaders) {
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

    private Optional<Row> emitOneRowWithPivotAndType(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotEntry pivotEntry) {
        if (!orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
            return Optional.empty();
        }

        final var newRow = new Row(this.tmpHeaders.size());
        for (final var tmpHeader : this.tmpHeaders) {
            if (tmpHeader instanceof PivotKeyHeader) {

                final var orgHeaders = orgTable.findAllHeaders(tmpHeader);
                if (orgHeaders.size() > 0) {
                    final var ci = tmpHeader.getColumnIndex();
                    newRow.set(ci + 0, pivotEntry.getPivotValue());
                    newRow.set(ci + 1, pivotEntry.getTypeValue());
                    newRow.set(ci + 2, orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).getValue());
                }

            } else {
                this.emitAllCells(graph, orgTable, orgRow, rowGroup, tmpHeader, newRow);
            }
        }

        return Optional.of(newRow);
    }

    private Optional<Row> emitOneRowWithPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup, final PivotEntry pivotEntry) {
        if (!orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
            return Optional.empty();
        }

        final var newRow = new Row(this.tmpHeaders.size());
        for (final var tmpHeader : this.tmpHeaders) {
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

    private void emitAllCells(final BaseTableGraph graph, final DataTable orgTable, final BaseRow orgRow,
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
}
