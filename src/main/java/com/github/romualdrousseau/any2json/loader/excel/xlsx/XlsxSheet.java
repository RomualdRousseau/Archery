package com.github.romualdrousseau.any2json.loader.excel.xlsx;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.github.romualdrousseau.any2json.base.PatcheableSheetStore;
import com.github.romualdrousseau.shuju.bigdata.DataFrame;
import com.github.romualdrousseau.shuju.bigdata.DataFrameWriter;
import com.github.romualdrousseau.shuju.bigdata.Row;
import com.github.romualdrousseau.shuju.strings.StringUtils;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XlsxSheet extends PatcheableSheetStore implements Closeable {

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final int BATCH_SIZE = 100000;

    public XlsxSheet(final String name, final InputStream sheetData, final SharedStrings sharedStrings,
            final StylesTable styles) {
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
            final var contentHandler = new ContentHandler(writer);
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(this.sheetData));
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
            final var cells = this.rows.getRow(n);
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
            final var cells = this.rows.getRow(n);
            return cells != null && colIndex < cells.size() ? StringUtils.cleanToken(cells.get(colIndex)) : null;
        }
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (mergedRegions.size() == 0) {
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

    private class ContentHandler extends DefaultHandler {

        public ContentHandler(final DataFrameWriter dataFrameWriter) {
            this.rows = dataFrameWriter;
        }

        public DataFrame getRows() throws IOException {
            return this.rows.getDataFrame();
        }

        @Override
        public void startElement(final String uri, final String localName, final String name,
                final Attributes attributes) {
            if ("row".equals(name)) {
                assert (attributes.getValue("r") != null) : "Row malformed without ref";
                this.fillMissingRows(Integer.valueOf(attributes.getValue("r")) - 1);
                this.row = new ArrayList<String>();
                this.prevCell = null;
                this.currCell = null;
            } else if ("c".equals(name)) {
                assert (attributes.getValue("r") != null) : "Cell malformed without ref";
                this.prevCell = this.currCell;
                this.currCell = new Cell();
                this.currCell.address = new CellAddress(attributes.getValue("r"));
                this.currCell.type = this.getCellTypeFromString(attributes.getValue("t"));
                this.currCell.style = this.getCellStyleFromString(attributes.getValue("s"));
            } else if ("v".equals(name)) {
                this.startValue = true;
                this.inlineStr = false;
                this.currCell.value = "";
            } else if ("t".equals(name)) {
                this.startValue = true;
                this.inlineStr = true;
                this.currCell.value = "";
            } else if ("mergeCell".equals(name) && attributes.getValue("ref") != null) {
                mergedRegions.add(CellRangeAddress.valueOf(attributes.getValue("ref")));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String name) {
            try {
                if ("row".equals(name)) {
                    this.rows.write(Row.of(this.row.toArray(String[]::new)));
                } else if ("c".equals(name)) {
                    this.fillMissingCells();
                    if (this.processCellData(this.currCell)) {
                        this.row.add(this.currCell.value);
                    } else {
                        this.row.add(null);
                    }
                } else if ("v".equals(name)) {
                    this.startValue = false;
                } else if ("t".equals(name)) {
                    this.startValue = false;
                }
            } catch (final IOException x) {
                throw new UncheckedIOException(x);
            }
        }

        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException {
            if (this.startValue) {
                this.currCell.value += new String(ch, start, length);
            }
        }

        private void fillMissingRows(final int n) {
            try {
                while (rows.getRowCount() < n) {
                    this.rows.write(Row.Empty);
                }
            } catch (final IOException x) {
                throw new UncheckedIOException(x);
            }
        }

        private CellType getCellTypeFromString(final String typeStr) {
            if (typeStr == null) {
                return CellType.BLANK;
            } else if (typeStr.equals("s") || typeStr.equals("inlineStr")) {
                return CellType.STRING;
            } else if (typeStr.equals("b")) {
                return CellType.BOOLEAN;
            } else if (typeStr.equals("e")) {
                return CellType.ERROR;
            } else {
                return CellType.NUMERIC;
            }
        }

        private XSSFCellStyle getCellStyleFromString(final String styleStr) {
            if (styleStr != null) {
                return styles.getStyleAt(Integer.valueOf(styleStr));
            } else if (styles.getNumCellStyles() > 0) {
                return styles.getStyleAt(0);
            } else {
                return null;
            }
        }

        private void fillMissingCells() {
            final int prevColumn = (this.prevCell == null) ? 0 : (this.prevCell.address.getColumn() + 1);
            for (int i = prevColumn; i < this.currCell.address.getColumn(); i++) {
                if (i < this.row.size()) {
                    this.row.set(i, null);
                } else {
                    this.row.add(null);
                }
            }
        }

        private boolean processCellData(final Cell cell) {
            cell.type = (cell.value == null && cell.type.equals(CellType.NUMERIC)) ? CellType.BLANK : cell.type;
            cell.type = (cell.value != null && cell.type.equals(CellType.BLANK)) ? CellType.NUMERIC : cell.type;

            if (cell.type.equals(CellType.STRING)) {
                if (!this.inlineStr && cell.value != null) {
                    cell.value = sharedStrings.getItemAt(Integer.valueOf(cell.value)).toString();
                }
                cell.value = StringUtils.cleanToken(cell.value);
            } else if (cell.type.equals(CellType.BOOLEAN)) {
                cell.value = cell.value.equals("1") ? "TRUE" : "FALSE";
            } else if (cell.type.equals(CellType.NUMERIC)) {
                try {
                    final double d = Double.valueOf(cell.value);
                    if (DateUtil.isADateFormat(cell.style.getDataFormat(), cell.style.getDataFormatString())
                            && DateUtil.isValidExcelDate(d)) {
                        cell.value = DATE_FORMATTER.format(DateUtil.getJavaDate(d));
                    }
                } catch (final NumberFormatException x) {
                    cell.type = CellType.STRING;
                    cell.value = StringUtils.cleanToken(cell.value);
                }
            } else if (cell.type.equals(CellType.BLANK)) {
                cell.value = "";
            }

            cell.decorated = hasDecoration(cell);

            return this.hasData(cell) || cell.decorated;
        }

        private boolean hasData(final Cell cell) {
            if (!cell.type.equals(CellType.BLANK) && cell.value != null && !cell.value.isEmpty()) {
                return true;
            }
            return false;
        }

        private boolean hasDecoration(final Cell cell) {
            if (cell.style == null) {
                return false;
            }

            // Keep cell with borders
            if (!cell.style.getBorderLeft().equals(BorderStyle.NONE)
                    && !cell.style.getBorderRight().equals(BorderStyle.NONE)
                    && !cell.style.getBorderTop().equals(BorderStyle.NONE)
                    && !cell.style.getBorderBottom().equals(BorderStyle.NONE)) {
                return true;
            }

            // Keep cell with a colored (not automatic and not white) pattern
            final XSSFColor bkcolor = (XSSFColor) cell.style.getFillBackgroundColorColor();
            if (bkcolor != null && bkcolor.getIndexed() != IndexedColors.AUTOMATIC.index
                    && (bkcolor.getARGBHex() == null || !bkcolor.getARGBHex().equals("FFFFFFFF"))) {
                return true;
            }

            // Keep cell with a colored (not automatic and not white) background
            final XSSFColor fgcolor = (XSSFColor) cell.style.getFillForegroundColorColor();
            if (fgcolor != null && fgcolor.getIndexed() != IndexedColors.AUTOMATIC.index
                    && (fgcolor.getARGBHex() == null || !fgcolor.getARGBHex().equals("FFFFFFFF"))) {
                return true;
            }

            return false;
        }

        private class Cell {
            CellAddress address;
            CellType type;
            CellStyle style;
            String value;
            boolean decorated;
        }

        private ArrayList<String> row;
        private boolean startValue;
        private boolean inlineStr;
        private Cell currCell;
        private Cell prevCell;
        private final DataFrameWriter rows;
    }

    private final String name;
    private final InputStream sheetData;
    private final StylesTable styles;
    private final SharedStrings sharedStrings;
    private final List<CellRangeAddress> mergedRegions = new ArrayList<>();
    private DataFrame rows;
    private boolean dataLoaded;
}
