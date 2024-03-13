package com.github.romualdrousseau.any2json.loader.pdf;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;

import technology.tabula.ObjectExtractor;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

class PdfSheet extends PatcheableSheetStore implements Closeable {

    private static final int BATCH_SIZE = 50000;
    private static final int MAX_COLUMNS = 100;

    private final String name;

    private PDDocument reader;
    private DataFrame rows;

    public PdfSheet(final String name, final PDDocument reader) {
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
        return this.rows.getColumnCount(rowIndex) - 1;
    }

    @Override
    public int getLastRowNum() {
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
            return StringUtils.cleanToken(this.getCellAt(colIndex, rowIndex));
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        return 1;
    }

    public PdfSheet ensureDataLoaded() {
        if (this.rows != null) {
            return this;
        }
        try (final var writer = new DataFrameWriter(BATCH_SIZE, MAX_COLUMNS);) {
            this.rows = this.processRows(this.reader, writer);
            this.reader.close();
            this.reader = null;
            return this;
        } catch (final IOException x) {
            return this;
        }
    }

    private DataFrame processRows(final PDDocument reader, final DataFrameWriter writer) throws IOException {
        final var sea = new SpreadsheetExtractionAlgorithm();
        final var bea = new BasicExtractionAlgorithm();
        try (final var extractor = new ObjectExtractor(reader)) {
            final var pi = extractor.extract();
            while (pi.hasNext()) {
                final var page = pi.next();
                final var tables = sea.isTabular(page) ? sea.extract(page) : bea.extract(page);
                for (final var table : tables) {
                    final var rows = table.getRows();
                    for (final var row : rows) {
                        final var cells = new ArrayList<String>();
                        for (final var cell : row) {
                            cells.add(StringUtils.cleanToken(cell.getText()));
                        }
                        writer.write(Row.of(cells.toArray(String[]::new)));
                    }
                    writer.write(Row.of(""));
                    writer.write(Row.of(""));
                }
            }
        }
        return writer.getDataFrame();
    }

    private String getCellAt(final int colIndex, final int rowIndex) {
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
