package com.github.romualdrousseau.archery.intelli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.header.PivotKeyHeader;

public class IntelliTable extends DataTable {

    private static final int BATCH_SIZE = 10000;

    private final DataFrameWriter writer;
    private final DataFrame rows;

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root, final boolean headerAutoNameEnabled) {
        super(sheet);
        try {
            final var pivotEntryTypes = this.collectPivotEntryTypes(root);
            final var headers = this.collectHeaders(root, pivotEntryTypes);
            final var pivotKeyHeader = this.findKeyPivotHeader(headers);
            final var pivotTypeHeader = this.findPivotTypeHeader(headers);
            final var strategy = this.getStrategy(pivotKeyHeader);
            this.writer = new DataFrameWriter(BATCH_SIZE, headers.size());
            this.buildTables(root, strategy, headers, pivotKeyHeader, pivotTypeHeader);
            this.rows = this.writer.getDataFrame();
            this.finalizeHeaders(headers, headerAutoNameEnabled);
        } catch (final IOException x) {
            throw new UncheckedIOException(x);
        }
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

    private HashSet<String> collectPivotEntryTypes(final BaseTableGraph root) {
        final var pivotEntryTypes = new HashSet<String>();
        root.parse(e -> e.getTable().headers().forEach(h -> this.addPivotEntryTypes((BaseHeader) h, pivotEntryTypes)));
        return pivotEntryTypes;
    }

    private List<BaseHeader> collectHeaders(final BaseTableGraph root, final Set<String> pivotEntryTypes) {
        final var tmpHeaders = new ArrayList<BaseHeader>();
        root.parse(e -> e.getTable().headers()
                .forEach(h -> this.collectHeader(tmpHeaders, (BaseHeader) h, pivotEntryTypes)));
        return tmpHeaders;
    }

    private PivotKeyHeader findKeyPivotHeader(final List<BaseHeader> headers) {
        for (final var header : headers) {
            if (header instanceof PivotKeyHeader) {
                return (PivotKeyHeader) header;
            }
        }
        return null;
    }

    private DataTableHeader findPivotTypeHeader(final List<BaseHeader> headers) {
        for (final var header : headers) {
            if (header.isPivotTypeHeader()) {
                return (DataTableHeader) header;
            }
        }
        return null;
    }

    private IntelliTableStrategy getStrategy(final PivotKeyHeader pivotKeyHeader) {
        if (pivotKeyHeader == null) {
            return new IntelliTableStrategyWithoutPivot();
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            return new IntelliTableStrategyWithPivotTypeAndValue();
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            return new IntelliTableStrategyWithPivotType();
        } else {
            return new IntelliTableStrategyWithPivot();
        }
    }

    private void buildTables(final BaseTableGraph root, final IntelliTableStrategy strategy,
            final List<BaseHeader> headers,
            final PivotKeyHeader pivotKeyHeader,
            final DataTableHeader pivotTypeHeader) {
        root.parseIf(
                e -> this.emitAllRowsForOneTable(strategy, headers, (BaseTableGraph) e, (DataTable) e.getTable(),
                        pivotKeyHeader,
                        pivotTypeHeader),
                e -> e instanceof BaseTableGraph && e.getTable() instanceof DataTable);
        this.setLoadCompleted(true);
    }

    private void finalizeHeaders(final List<BaseHeader> headers, final boolean headerAutoNameEnabled) {
        headers.forEach(h -> this.addHeader(new IntelliHeader(h, !headerAutoNameEnabled)));
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

    private void collectHeader(final List<BaseHeader> headers, final BaseHeader header,
            final Set<String> pivotEntryTypes) {
        if (headers.contains(header)) {
            return;
        }
        if (header instanceof PivotKeyHeader) {
            this.addPivotHeader(headers, header, pivotEntryTypes);
        } else {
            this.addHeader(headers, header.clone(), true);
        }
    }

    private void addPivotHeader(final List<BaseHeader> headers, final BaseHeader header,
            final Set<String> pivotEntryTypes) {
        final var pivotHeader = (PivotKeyHeader) header.clone();
        if (!pivotEntryTypes.isEmpty()) {
            pivotHeader.setEntryTypeValues(pivotEntryTypes);
        }
        if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            this.addHeaderWithPivotTypeAndValue(headers, pivotHeader);
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            this.addHeaderWithPivotType(headers, pivotHeader);
        } else {
            this.addHeaderWithPivot(headers, pivotHeader);
        }
    }

    private void addHeaderWithPivotTypeAndValue(final List<BaseHeader> headers, final PivotKeyHeader pivotHeader) {
        this.addHeader(headers, pivotHeader, false);
        pivotHeader.getEntryTypeValues().forEach(
                x -> this.addHeader(headers, pivotHeader.getPivotTypeHeader().clone().setName(x),
                        false));
    }

    private void addHeaderWithPivotType(final List<BaseHeader> headers, final PivotKeyHeader pivotHeader) {
        this.addHeader(headers, pivotHeader, false);
        this.addHeader(headers, pivotHeader.getPivotTypeHeader().clone(), false);
        this.addHeader(headers, pivotHeader.getPivotValueHeader().clone(), false);
    }

    private void addHeaderWithPivot(final List<BaseHeader> headers, final PivotKeyHeader pivotHeader) {
        this.addHeader(headers, pivotHeader, false);
        this.addHeader(headers, pivotHeader.getPivotValueHeader().clone(), false);
    }

    private void addHeader(final List<BaseHeader> headers, final BaseHeader newHeader,
            final boolean columnEmpty) {
        newHeader.setTable(this);
        newHeader.setColumnIndex(headers.size());
        newHeader.setColumnEmpty(columnEmpty);
        headers.add(newHeader);
    }

    private void emitAllRowsForOneTable(final IntelliTableStrategy strategy, final List<BaseHeader> headers,
            final BaseTableGraph graph,
            final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader) {
        if (orgTable.getNumberOfRowGroups() == 0) {
            this.emitAllRowsForOneTableWithoutRowGroups(strategy, headers, graph, orgTable, pivotKeyHeader,
                    pivotTypeHeader);
        } else {
            this.emitAllRowsForOneTableWithRowGroups(strategy, headers, graph, orgTable, pivotKeyHeader,
                    pivotTypeHeader);
        }
    }

    private void emitAllRowsForOneTableWithoutRowGroups(final IntelliTableStrategy strategy,
            final List<BaseHeader> headers,
            final BaseTableGraph graph,
            final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader) {
        for (final var orgRow : orgTable.rows()) {
            this.addRows(strategy.emitAllRowsForOneRow(headers, graph, orgTable, (BaseRow) orgRow,
                    pivotKeyHeader, pivotTypeHeader, null));
        }
    }

    private void emitAllRowsForOneTableWithRowGroups(final IntelliTableStrategy strategy,
            final List<BaseHeader> headers,
            final BaseTableGraph graph,
            final DataTable orgTable,
            final PivotKeyHeader pivotKeyHeader, final DataTableHeader pivotTypeHeader) {
        for (final RowGroup rowGroup : orgTable.rowGroups()) {
            for (int i = 0; i < rowGroup.getNumberOfRows(); i++) {
                if (rowGroup.getRow() + i < orgTable.getNumberOfRows()) {
                    final var orgRow = orgTable.getRowAt(rowGroup.getRow() + i);
                    this.addRows(strategy.emitAllRowsForOneRow(headers, graph, orgTable, orgRow,
                            pivotKeyHeader,
                            pivotTypeHeader,
                            rowGroup));
                }
            }
        }

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
