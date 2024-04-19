package com.github.romualdrousseau.any2json.loader.pdf;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.TextElement;
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
                if (sea.isTabular(page)) {
                    this.processRowsTabular(sea, page, writer);
                } else {
                    this.processRowsLatice(bea, page, writer);
                }
                writer.write(Row.of(""));
                writer.write(Row.of(""));
            }
        }
        return writer.getDataFrame();
    }

    private void processRowsTabular(final SpreadsheetExtractionAlgorithm sea, final Page page, final DataFrameWriter writer) throws IOException {
        final var tables = sea.extract(page);
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

    private void processRowsLatice(BasicExtractionAlgorithm bea, Page page, DataFrameWriter writer) throws IOException {
        final var tables = bea.extract(page);
        for (final var table : tables) {
            final var rows = table.getRows();
            var isPreviousTableRow = false;
            for (final var row : rows) {
                final var elements = this.getElements(row);
                if (elements.size() > 0) {
                    if (this.isTableRow(elements, isPreviousTableRow)) {
                        if (!isPreviousTableRow) {
                            writer.write(Row.of(""));
                            writer.write(Row.of(""));
                        }
                        final var cells = new ArrayList<String>();
                        for (final var text : this.getCells(elements)) {
                            cells.add(StringUtils.cleanToken(text));
                        }
                        writer.write(Row.of(this.getCells(elements)));
                        isPreviousTableRow = true;
                    } else {
                        if (isPreviousTableRow) {
                            writer.write(Row.of(""));
                            writer.write(Row.of(""));
                        }
                        writer.write(Row.of(StringUtils.cleanToken(this.getText(elements))));
                        isPreviousTableRow = false;
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private List<TextElement> getElements(final List<RectangularTextContainer> row) {
        final var elements = new ArrayList<TextElement>();
        for (final var cell : row) {
            for (final var element : cell.getTextElements()) {
                if (element instanceof TextElement){
                    elements.add((TextElement) element);
                }
            }
        }
        return elements;
    }

    private boolean isTableRow(List<TextElement> elements, boolean isPreviousTableRow) {
        var margin = Math.floor(Math.max(elements.get(0).getX() / elements.get(0).getWidthOfSpace() - 4, 0) / 4);
        var separators = 0.0;
        // var symbols = 0.0;

        var x = elements.get(0).getX();
        for (final TextElement element: elements) {
            // if (element.getText().isBlank()) {
            //     symbols += 1.0;
            // }
            separators += Math.floor(Math.max((element.getX() - x) / element.getWidthOfSpace() - 4, 0) / 4);
            x = element.getX();
        }

        // Very naive Naive Bayes
        final var pRow = pRowMargin(margin) * pRowSeparators(separators);
        final var pNotRow = pNotRowMargin(margin) * pNotRowSeparators(separators);
        return (!isPreviousTableRow) ? pRow > pNotRow : pRow >= pNotRow;
    }

    private String[] getCells(List<TextElement> elements) {
        var x = 0.0;
        var text = "";
        for (final TextElement element: elements) {
            final var spacing = Math.max((element.getX() - x) / element.getWidthOfSpace() - 4, 0);
            for (int i = 0; i < spacing; i++) {
                text += " ";
            }
            text += element.getText();
            x = element.getX();
        }
        return text.split("   +");
    }

    private String getText(List<TextElement> elements) {
        var text = "";
        for (final TextElement element: elements) {
            text += element.getText();
        }
        return text;
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

    private float pRowMargin(final double margin) {
        return margin > 6.0 ? 1.0f : 0.0f;
    }

    private float pNotRowMargin(final double margin) {
        return 1.0f - pRowMargin(margin);
    }

    private float pRowSeparators(final double separators) {
        return separators > 0.0 ? 1.0f : 0.0f;
    }

    private float pNotRowSeparators(final double separators) {
        return 1.0f - this.pRowSeparators(separators);
    }
}
