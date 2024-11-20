package com.github.romualdrousseau.archery.intelli;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;

import com.github.romualdrousseau.archery.PivotOption;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.RowGroup;
import com.github.romualdrousseau.archery.header.DataTableTypeHeader;
import com.github.romualdrousseau.archery.header.PivotEntry;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;
import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class IntelliTable extends DataTable {

    private static final int BATCH_SIZE = 10000;

    private final ArrayList<BaseHeader> tmpHeaders = new ArrayList<>();
    private final DataFrameWriter writer;
    private final DataFrame rows;
    private final Set<String> typesValues = new HashSet<>();

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root, final boolean headerAutoNameEnabled) {
        super(sheet);

        // Collect headers

        root.parse(e -> e.getTable().headers().forEach(h -> this.addTmpHeader((BaseHeader) h)));

        // Build tables

        try {
            this.writer = new DataFrameWriter(BATCH_SIZE, this.tmpHeaders.size());
            final var pivotHeader = this.findPivotHeader();
            final var typeHeader = this.findDataTableTypeHeader();
            root.parseIf(
                    e -> this.buildRowsForOneTable((BaseTableGraph) e, (DataTable) e.getTable(), pivotHeader,
                            typeHeader),
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
                pivot.getEntryTypes()
                        .forEach(x -> this.addHeaderIntoTmpHeaders(pivot.getPivotType().clone().setName(x), false));
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
            final PivotKeyHeader pivotHeader, final DataTableTypeHeader typeHeader) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            for (final var orgRow : orgTable.rows()) {
                final var newRows = buildRowsForOneRow(graph, orgTable, (BaseRow) orgRow, pivotHeader, typeHeader,
                        null);
                this.addRows(newRows);
            }
        } else {
            for (final RowGroup rowGroup : orgTable.rowGroups()) {
                for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                    if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                        final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                        final var newRows = buildRowsForOneRow(graph, orgTable, orgRow, pivotHeader, typeHeader,
                                rowGroup);
                        this.addRows(newRows);
                    }
                }
            }
        }
    }

    private List<Row> buildRowsForOneRow(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow,
            final PivotKeyHeader pivotHeader, final DataTableTypeHeader typeHeader, final RowGroup rowGroup) {
        final var newRows = new ArrayList<Row>();
        if (orgRow.isIgnored()) {
            return newRows;
        }
        if (pivotHeader == null) {
            this.buildOneRowWithoutPivot(graph, orgTable, orgRow, rowGroup).ifPresent(newRows::add);
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            final var typeValue = this.findTypeValue(orgRow, pivotHeader, typeHeader);
            pivotHeader.getEntryValues()
                    .forEach(x -> this
                            .buildOneRowWithPivotTypeAndValue(graph, orgTable, orgRow, pivotHeader, x, typeValue,
                                    rowGroup)
                            .ifPresent(newRows::add));
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            pivotHeader.getEntries()
                    .forEach(x -> this.buildOneRowWithPivotAndType(graph, orgTable, orgRow, x, rowGroup)
                            .ifPresent(newRows::add));
        } else {
            pivotHeader.getEntries()
                    .forEach(x -> this.buildOneRowWithPivot(graph, orgTable, orgRow, x, rowGroup)
                            .ifPresent(newRows::add));
        }
        return newRows;
    }

    private Optional<Row> buildOneRowWithoutPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
        }
        return Optional.of(newRow);
    }

    private Optional<Row> buildOneRowWithPivotTypeAndValue(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotKeyHeader pivotHeader, final String value, final String typeValue,
            final RowGroup rowGroup) {
        final var newRow = new Row(this.tmpHeaders.size());
        boolean hasPivotedValues = false;
        for (final var abstractHeader : this.tmpHeaders) {
            final var orgHeaders = orgTable.findAllHeaders(abstractHeader);
            if (abstractHeader instanceof PivotKeyHeader) {
                if (orgHeaders.size() > 0) {
                    newRow.set(abstractHeader.getColumnIndex(), value);
                    int i = 1;
                    for (final var tv : pivotHeader.getEntryTypes()) {
                        final var ci = abstractHeader.getColumnIndex() + i;
                        hasPivotedValues |= pivotHeader.getEntries().stream()
                                .filter(x -> x.getValue().equals(value)
                                        && (typeValue != null && tv.equals(typeValue)
                                                || typeValue == null && x.getTypeValue().equals(typeValue)))
                                .findFirst()
                                .map(x -> {
                                    newRow.set(ci, orgRow.getCellAt(x.getCell().getColumnIndex()).getValue());
                                    return orgRow.getCellAt(x.getCell().getColumnIndex()).hasValue();
                                })
                                .orElse(false);
                        i++;
                    }
                }
            } else {
                this.generateCells(graph, orgHeaders, abstractHeader, rowGroup, orgRow, newRow);
            }
        }
        return hasPivotedValues ? Optional.of(newRow) : Optional.empty();
    }

    private Optional<Row> buildOneRowWithPivotAndType(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        if (!orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
            return Optional.empty();
        }
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
        return Optional.of(newRow);
    }

    private Optional<Row> buildOneRowWithPivot(final BaseTableGraph graph, final DataTable orgTable,
            final BaseRow orgRow, final PivotEntry pivotEntry, final RowGroup rowGroup) {
        if (!orgRow.getCellAt(pivotEntry.getCell().getColumnIndex()).hasValue()) {
            return Optional.empty();
        }
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
        return Optional.of(newRow);
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

    private void generateCell(final BaseHeader abstractHeader, final String prevValue, final String currValue,
            final Row newRow) {
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

    private DataTableTypeHeader findDataTableTypeHeader() {
        for (final var header : this.tmpHeaders) {
            if (header instanceof DataTableTypeHeader) {
                return (DataTableTypeHeader) header;
            }
        }
        return null;
    }
    private String findTypeValue(final BaseRow orgRow, final PivotKeyHeader pivotHeader,
            final DataTableTypeHeader typeHeader) {
        if (typeHeader == null) {
            return null;
        }

        final var typeValue = typeHeader.getCellAtRow(orgRow).getValue();
        if (this.typesValues.contains(typeValue)) {
            return typeValue;
        }

        this.typesValues.add(typeValue);

        final var entries = new ArrayList<PivotEntry>();
        this.typesValues.forEach(x -> {
            pivotHeader.getEntries().stream().map(y -> y.clone().setTypeValue(x)).forEach(entries::add);
        });
        pivotHeader.getEntries().clear();
        pivotHeader.getEntries().addAll(entries);

        return typeValue;
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
