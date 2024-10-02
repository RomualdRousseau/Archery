package com.github.romualdrousseau.archery.loader.parquet;

import java.io.Closeable;
import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.hadoop.ParquetReader;

import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.bigdata.DataFrame;
import com.github.romualdrousseau.archery.commons.bigdata.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.bigdata.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

class ParquetSheet extends PatcheableSheetStore implements Closeable {

    private static final int BATCH_SIZE = 50000;

    private final String name;

    private ParquetReader<GenericRecord> reader;
    private DataFrame rows = null;

    public ParquetSheet(final String name, final ParquetReader<GenericRecord> reader) {
        this.name = name;
        this.reader = reader;
        this.rows = null;
    }

    @Override
    public void close() throws IOException {
        if (this.rows != null) {
            this.rows.close();
        }
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        if (this.rows == null) {
            return 0;
        }
        return this.rows.getRow(rowIndex).size() - 1;
    }

    @Override
    public int getLastRowNum() {
        if (this.rows == null) {
            return 0;
        }
        return this.rows.getRowCount() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return true;
        } else {
            return this.getCellAt(colIndex, rowIndex) != null;
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var patchCell = this.getPatchCell(colIndex, rowIndex);
        if (patchCell != null) {
            return patchCell;
        } else {
            return this.getCellAt(colIndex, rowIndex);
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
    }

    public ParquetSheet ensureDataLoaded() {
        if (this.rows != null) {
            return this;
        }
        try (final var writer = new DataFrameWriter(BATCH_SIZE)) {
            this.rows = this.processRows(this.reader, writer);
            this.reader.close();
            this.reader = null;
            return this;
        } catch (IOException x) {
            return this;
        }
    }

    private DataFrame processRows(final ParquetReader<GenericRecord> reader, final DataFrameWriter writer)
            throws IOException {
        var firstPass = true;
        for (GenericRecord record; (record = reader.read()) != null;) {
            if (firstPass) {
                writer.write(Row.of(parseHeader(record)));
                firstPass = false;
            }
            writer.write(Row.of(parseOneRecord(record)));
        }
        return writer.getDataFrame();
    }

    private String[] parseHeader(final GenericRecord record) {
        return record.getSchema().getFields().stream()
                .map(x -> StringUtils.cleanToken(x.name()))
                .toArray(String[]::new);
    }

    private String[] parseOneRecord(final GenericRecord record) {
        return record.getSchema().getFields().stream()
                .map(x -> {
                    final var value = record.get(x.pos());
                    return (value != null) ? StringUtils.cleanToken(value.toString()) : "";
                })
                .toArray(String[]::new);
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
        if (this.rows == null) {
            return null;
        }

        if (rowIndex >= this.rows.getRowCount()) {
            return null;
        }

        final Row row = this.rows.getRow(rowIndex);

        if (colIndex >= row.size()) {
            return null;
        }

        return row.get(colIndex);
    }
}
