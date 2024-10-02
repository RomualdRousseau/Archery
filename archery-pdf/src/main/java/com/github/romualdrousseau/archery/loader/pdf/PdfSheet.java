package com.github.romualdrousseau.archery.loader.pdf;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.bigdata.DataFrame;
import com.github.romualdrousseau.archery.commons.bigdata.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.bigdata.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.TextElement;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

class PdfSheet extends PatcheableSheetStore implements Closeable {

    private static final int BATCH_SIZE = 50000;
    private static final int MAX_COLUMNS = 100;
    private static final int LATICE_SPACES = 3; // Number of spaces to be consider as a column separator
    private static final int LATICE_MARGINS = 1; // Minimum margins to consider the begin of a row
    private static final int LATICE_COLUMN_SEPARATORS = 4; // Number of column separators to consider it is a row

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

    private void processRowsTabular(final SpreadsheetExtractionAlgorithm sea, final Page page,
            final DataFrameWriter writer) throws IOException {
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

    private void processRowsLatice(final BasicExtractionAlgorithm bea, final Page page, final DataFrameWriter writer)
            throws IOException {
        final var tableRows = new ArrayList<String>();
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
                        tableRows.add(this.getTableRow(elements));
                        isPreviousTableRow = true;
                    } else {
                        if (tableRows.size() > 0) {
                            this.processTableLatice(tableRows, writer);
                            tableRows.clear();
                        }
                        if (isPreviousTableRow) {
                            writer.write(Row.of(""));
                            writer.write(Row.of(""));
                        }
                        writer.write(Row.of(StringUtils.cleanToken(this.getText(elements))));
                        isPreviousTableRow = false;
                    }
                } else {
                    isPreviousTableRow = false;
                }
            }
        }
        if (tableRows.size() > 0) {
            this.processTableLatice(tableRows, writer);
            tableRows.clear();
        }
    }

    private void processTableLatice(final ArrayList<String> rows, final DataFrameWriter writer) throws IOException {
        final var tabs = new ArrayList<Integer>();
        final int maxLength = rows.stream().mapToInt(x -> x.length()).max().getAsInt();

        var last_tab = -1;
        for (int i = 0; i < maxLength; i++) {
            final int tab = i;
            final var allBlanks = rows.stream().allMatch(x -> tab >= x.length() || this.isLaticeSpace(x.charAt(tab)));
            if (allBlanks) {
                if (last_tab >= 0 && (tab - last_tab) == 1) {
                    tabs.remove(tabs.size() - 1);
                }
                tabs.add(tab);
                last_tab = tab;
            }
        }
        tabs.add(maxLength - 1);

        for (final var row : rows) {
            final var cells = new ArrayList<String>();
            for (int i = 0; i < tabs.size() - 1; i++) {
                final var begin = tabs.get(i);
                if (begin < row.length()) {
                    final var end = tabs.get(i + 1);
                    if (end < row.length() - 1) {
                        cells.add(StringUtils.cleanToken(row.substring(begin, end)));
                    } else {
                        cells.add(StringUtils.cleanToken(row.substring(begin)));
                    }
                } else {
                    cells.add("");
                }
            }
            writer.write(Row.of(cells.toArray(new String[] {})));
        }
    }

    @SuppressWarnings("rawtypes")
    private List<TextElement> getElements(final List<RectangularTextContainer> row) {
        final var elements = new ArrayList<TextElement>();
        for (final var cell : row) {
            for (final var element : cell.getTextElements()) {
                if (element instanceof TextElement) {
                    elements.add((TextElement) element);
                }
            }
        }
        return elements;
    }

    private boolean isTableRow(final List<TextElement> elements, final boolean isPreviousTableRow) {
        final var margins = (int) Math
                .floor(Math.max(elements.get(0).getX() / elements.get(0).getWidthOfSpace(), 0) / LATICE_SPACES);

        var separators = 0;
        var x = elements.get(0).getX();
        for (final TextElement element : elements) {
            final var spacing = Math.max((element.getX() - x) / element.getWidthOfSpace() - LATICE_SPACES, 0);
            if (spacing > 0) {
                separators++;
            }
            x = element.getX();
        }

        final var pRow = 0.5 * pRowMargin(margins) + 0.5 * pRowSeparators(separators);
        return (!isPreviousTableRow) ? pRow == 1.0 : pRow >= 0.5; // Give a bit of lax if we are in a table, i.e. the
                                                                  // previous row was a table row
    }

    private String getTableRow(final List<TextElement> elements) {
        var text = "";
        for (final TextElement element : elements) {
            final var spacing = Math.max(element.getX() / element.getWidthOfSpace() - 1, 0) - text.length();
            for (int i = 0; i < spacing; i++) {
                text += " ";
            }
            text += element.getText();
        }
        return text;
    }

    private String getText(final List<TextElement> elements) {
        var text = "";
        for (final TextElement element : elements) {
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

    private float pRowMargin(final int margins) {
        return margins >= LATICE_MARGINS ? 1.0f : 0.0f;
    }

    private float pRowSeparators(final int separators) {
        return separators >= LATICE_COLUMN_SEPARATORS ? 1.0f : 0.0f;
    }

    private boolean isLaticeSpace(final char c) {
        return List.of(' ', '-', '_', '|').contains(c);
    }
}
