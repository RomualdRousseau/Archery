package com.github.romualdrousseau.archery.intelli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.archery.PivotOption;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseRow;
import com.github.romualdrousseau.archery.base.BaseSheet;
import com.github.romualdrousseau.archery.base.BaseTable;
import com.github.romualdrousseau.archery.base.BaseTableGraph;
import com.github.romualdrousseau.archery.base.DataTable;
import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;

public class IntelliTable extends DataTable {

    private static final int BATCH_SIZE = 10000;

    private final DataFrameWriter writer;
    private final DataFrame rows;

    public IntelliTable(final BaseSheet sheet, final BaseTableGraph root, final boolean headerAutoNameEnabled) {
        super(sheet);
        try {
            final var pivotEntryTypes = this.collectPivotTypes(root);
            final var strategy = this.getStrategy(pivotEntryTypes);
            this.collectHeaders(root, strategy, pivotEntryTypes);
            this.writer = new DataFrameWriter(BATCH_SIZE, strategy.getTmpHeaders().size());
            this.emitTables(root, strategy);
            this.finalizeHeaders(strategy, headerAutoNameEnabled);
            this.rows = this.writer.getDataFrame();
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

    private Set<String> collectPivotTypes(final BaseTableGraph root) {
        final var pivotEntryTypes = new HashSet<String>();
        root.parse(e -> e.getTable().headers().forEach(h -> this.addPivotEntryTypes((BaseHeader) h, pivotEntryTypes)));
        return pivotEntryTypes;
    }

    private void collectHeaders(final BaseTableGraph root, final IntelliTableStrategy strategy,
            final Set<String> pivotEntryTypes) {
        root.parse(e -> strategy.addTmpHeaders((BaseTable) e.getTable(), pivotEntryTypes));
    }

    private void emitTables(final BaseTableGraph root, final IntelliTableStrategy strategy) throws IOException {
        final var pivotKeyHeader = strategy.findKeyPivotHeader();
        final var pivotTypeHeader = strategy.findPivotTypeHeader();
        root.parseIf(
                e -> strategy.emitAllRowsForOneTable((BaseTableGraph) e, (DataTable) e.getTable(), pivotKeyHeader,
                        pivotTypeHeader, writer),
                e -> e instanceof BaseTableGraph && e.getTable() instanceof DataTable);
        this.setLoadCompleted(true);
    }

    private void finalizeHeaders(final IntelliTableStrategy strategy, final boolean headerAutoNameEnabled) {
        strategy.getTmpHeaders().forEach(h -> this.addHeader(new IntelliHeader(h, !headerAutoNameEnabled)));
    }

    private void addPivotEntryTypes(final BaseHeader header, final Set<String> pivotEntryTypes) {
        if (header.isPivotTypeHeader()) {
            pivotEntryTypes.addAll(StreamSupport.stream(header.getTable().rows().spliterator(), false)
                    .filter(x -> header.getCellAtRow(x).hasValue())
                    .map(x -> header.getCellAtRow(x).getValue())
                    .distinct()
                    .toList());
        }
    }

    private IntelliTableStrategy getStrategy(final Set<String> pivotEntryTypes) {
        if (pivotEntryTypes.isEmpty()) {
            return new IntelliTableStrategyWithoutPivot();
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE_AND_VALUE) {
            return new IntelliTableStrategyWithPivotTypeAndValue();
        } else if (this.getSheet().getPivotOption() == PivotOption.WITH_TYPE) {
            return new IntelliTableStrategyWithPivotType();
        } else {
            return new IntelliTableStrategyWithPivot();
        }
    }
}
