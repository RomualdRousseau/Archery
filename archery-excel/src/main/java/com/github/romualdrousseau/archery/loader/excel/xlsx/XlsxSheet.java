package com.github.romualdrousseau.archery.loader.excel.xlsx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.base.PatcheableSheetStore;
import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XlsxSheet extends PatcheableSheetStore implements Closeable {

    private static final int BATCH_SIZE = 50000;

    private final XlsxDocument document;
    private final String name;
    private final InputStream sheetData;
    private final StylesTable styles;
    private final SharedStrings sharedStrings;
    private final LRUMap<Integer, Row> cachedRows = new LRUMap<>();

    private List<CellRangeAddress> mergedRegions;
    private DataFrame rows;
    private boolean dataLoaded;

    public XlsxSheet(final XlsxDocument document, final String name, final InputStream sheetData,
            final SharedStrings sharedStrings,
            final StylesTable styles) {
        this.document = document;
        this.name = name;
        this.sheetData = sheetData;
        this.sharedStrings = sharedStrings;
        this.styles = styles;
    }

    @Override
    public void close() {
        try {
            if (this.rows != null) {
                this.rows.close();
                this.rows = null;
            }
        } catch (final IOException x) {
            // throw new UncheckedIOException(x);
        }
    }

    public XlsxSheet ensureDataLoaded() {
        if (this.dataLoaded) {
            return this;
        }
        try (final var writer = new DataFrameWriter(BATCH_SIZE)) {
            final var parserFactory = SAXParserFactory.newInstance();
            final var parser = parserFactory.newSAXParser().getXMLReader();
            final var timeSupport = this.document.getHints().contains(Document.Hint.INTELLI_TIME);
            final var contentHandler = new ContentHandler(writer, this.sharedStrings, this.styles, timeSupport);
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(this.sheetData));
            this.mergedRegions = contentHandler.getMergesRegions();
            this.rows = contentHandler.getRows();
        } catch (SAXException | IOException | ParserConfigurationException ignore) {
        } finally {
            try {
                this.dataLoaded = true;
                this.sheetData.close();
            } catch (final IOException x) {
                // throw new UncheckedIOException(x);
            }
        }
        return this;
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
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.rows.getRowCount()) {
            return false;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return true;
        } else {
            final var cells = this.cachedRows.computeIfAbsent(n, this.rows::getRow);
            return cells != null && colIndex < cells.size() && cells.get(colIndex) != null;
        }
    }

    @Override
    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final var n = this.getInternalMergeDown(colIndex, rowIndex);
        if (n >= this.rows.getRowCount()) {
            return null;
        }
        final var patchCell = this.getPatchCell(colIndex, n);
        if (patchCell != null) {
            return patchCell;
        } else {
            final var cells = this.cachedRows.computeIfAbsent(n, this.rows::getRow);
            return cells != null && colIndex < cells.size() ? cells.get(colIndex) : null;
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return 1;
        }
        int numberOfCells = 0;
        for (final CellRangeAddress region : this.mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
                numberOfCells = region.getLastColumn() - region.getFirstColumn();
                break;
            }
        }
        return numberOfCells + 1;
    }

    @Override
    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final String value, final boolean unmergeAll) {
        final String newCell;
        if (value == null) {
            newCell = this.getCellDataAt(colIndex1, rowIndex1);
        } else {
            newCell = value;
        }

        if (!unmergeAll) {
            this.unmergeCell(colIndex2, rowIndex2);
        }

        final var n2 = this.getInternalMergeDown(colIndex2, rowIndex2);
        this.addPatchCell(colIndex2, n2, newCell);
    }

    private void unmergeCell(final int colIndex, final int rowIndex) {
        final List<CellRangeAddress> regionsToRemove = new ArrayList<CellRangeAddress>();
        for (final CellRangeAddress region : this.mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
                regionsToRemove.add(region);
            }
        }
        for (final CellRangeAddress region : regionsToRemove) {
            this.mergedRegions.remove(region);
        }
    }

    private int getInternalMergeDown(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return rowIndex;
        }
        var rowToReturn = rowIndex;
        for (final var region : mergedRegions) {
            if (region.getLastRow() > region.getFirstRow() && rowIndex > region.getFirstRow()
                    && region.isInRange(rowIndex, colIndex)) {
                rowToReturn = region.getFirstRow();
                break;
            }
        }
        return rowToReturn;
    }
}
