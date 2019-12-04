package com.github.romualdrousseau.any2json.v2.loader.excel.xlsx;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;
import com.github.romualdrousseau.shuju.util.StringUtility;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XlsxSheet extends IntelliSheet implements RowTranslatable {

    public class ContentHandler extends DefaultHandler {

        class Cell {
            CellAddress address;
            CellType type;
            CellStyle style;
            String value;
        }

        @Override
        public void startElement(final String uri, final String localName, final String name,
                final Attributes attributes) {
            if ("row".equals(name)) {
                final int rowIndex = Integer.valueOf(attributes.getValue("r")) - 1;
                while (rows.size() < rowIndex) {
                    rows.add(new XlsxRow());
                }

                this.row = new XlsxRow();
                final String ht = attributes.getValue("ht");
                if (ht != null) {
                    this.row.height = Float.valueOf(ht) * 4.0f / 3.0f;
                }

                this.prevCell = null;
                this.currCell = null;
            } else if ("c".equals(name)) {
                this.prevCell = this.currCell;
                this.currCell = new Cell();
                this.currCell.address = new CellAddress(attributes.getValue("r"));
                this.currCell.type = this.getType(attributes.getValue("t"));
                this.currCell.style = this.getStyle(attributes.getValue("s"));
            } else if ("v".equals(name)) {
                this.startValue = true;
                this.currCell.value = "";
            } else if ("mergeCell".equals(name) && attributes.getValue("ref") != null) {
                mergedRegions.add(CellRangeAddress.valueOf(attributes.getValue("ref")));
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String name) {
            if ("row".equals(name)) {
                if (this.currCell != null) {
                    this.row.lastColumnNum = this.currCell.address.getColumn();
                }
                rows.add(this.row);
            } else if ("c".equals(name)) {
                final int prevColumn = (this.prevCell == null) ? 0 : (this.prevCell.address.getColumn() + 1);
                for (int i = prevColumn; i < this.currCell.address.getColumn(); i++) {
                    this.row.addCell(XlsxCell.Empty);
                }
                if (this.processCellData(this.currCell)) {
                    final XlsxCell cell = new XlsxCell();
                    cell.value = this.currCell.value;
                    this.row.addCell(cell);
                } else {
                    this.row.addCell(XlsxCell.Empty);
                }
            } else if ("v".equals(name)) {
                this.startValue = false;
            }
        }

        @Override
        public void characters(final char ch[], final int start, final int length) throws SAXException {
            if (this.startValue) {
                this.currCell.value += new String(ch, start, length);
            }
        }

        private CellType getType(final String typeStr) {
            if (typeStr == null) {
                return CellType.BLANK;
            } else if (typeStr.equals("s")) {
                return CellType.STRING;
            } else if (typeStr.equals("b")) {
                return CellType.BOOLEAN;
            } else if (typeStr.equals("e")) {
                return CellType.ERROR;
            } else {
                return CellType.NUMERIC;
            }
        }

        private XSSFCellStyle getStyle(final String styleStr) {
            if (styleStr != null) {
                return styles.getStyleAt(Integer.valueOf(styleStr));
            } else if (styles.getNumCellStyles() > 0) {
                return styles.getStyleAt(0);
            } else {
                return null;
            }
        }

        private boolean processCellData(final Cell cell) {
            cell.type = (cell.value != null && cell.type.equals(CellType.BLANK)) ? CellType.NUMERIC : cell.type;

            if (cell.type.equals(CellType.STRING)) {
                cell.value = StringUtility.cleanToken(sharedStrings.getItemAt(Integer.valueOf(cell.value)).toString());

            } else if (cell.type.equals(CellType.BOOLEAN)) {
                cell.value = cell.value.equals("1") ? "TRUE" : "FALSE";

            } else if (cell.type.equals(CellType.NUMERIC)) {
                final double d = Double.valueOf(cell.value);
                if (DateUtil.isADateFormat(cell.style.getDataFormat(), cell.style.getDataFormatString())
                        && DateUtil.isValidExcelDate(d)) {
                    cell.value = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.getJavaDate(d));
                }

            } else if (cell.type.equals(CellType.BLANK)) {
                cell.value = "";
            }

            return this.hasData(cell);
        }

        private boolean hasData(final Cell cell) {
            if (!cell.type.equals(CellType.BLANK) && !cell.value.isEmpty()) {
                return true;
            }

            if (cell.address.getRow() == 0) {
                return false;
            }

            // Keep cell with colored borders
            if (!cell.style.getBorderLeft().equals(BorderStyle.NONE)
                    && !cell.style.getBorderRight().equals(BorderStyle.NONE)
                    && !cell.style.getBorderTop().equals(BorderStyle.NONE)
                    && !cell.style.getBorderBottom().equals(BorderStyle.NONE)) {
                return true;
            }

            // Keep cell with a colored (not automatic and not white) pattern
            final Color bkcolor = cell.style.getFillBackgroundColorColor();
            if (bkcolor != null) {
                if (((XSSFColor) bkcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                        && (((XSSFColor) bkcolor).getARGBHex() == null
                                || !((XSSFColor) bkcolor).getARGBHex().equals("FFFFFFFF"))) {
                    return true;
                }
            }

            // Keep cell with a colored (not automatic and not white) background
            final Color fgcolor = cell.style.getFillForegroundColorColor();
            if (fgcolor != null) {
                if (((XSSFColor) fgcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                        && (((XSSFColor) fgcolor).getARGBHex() == null
                                || !((XSSFColor) fgcolor).getARGBHex().equals("FFFFFFFF"))) {
                    return true;
                }
            }

            return false;
        }

        private XlsxRow row;
        private boolean startValue;
        private Cell currCell;
        private Cell prevCell;
    }

    public XlsxSheet(final String name, final InputStream sheetData, final SharedStringsTable sharedStrings,
            final StylesTable styles) {
        this.rowTranslator = new RowTranslator(this);
        this.name = name;
        this.sheetData = sheetData;
        this.sharedStrings = sharedStrings;
        this.styles = styles;
    }

    public Sheet ensureDataLoaded() {
        try {
            final XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(new ContentHandler());
            parser.parse(new InputSource(this.sheetData));
            return this;
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(final int rowIndex) {
        return this.rows.get(rowIndex).lastColumnNum;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.size() - 1;
    }

    @Override
    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final XlsxCell cell = this.getCellAt(colIndex, rowIndex);
        return cell != null && cell.value != null;
    }

    @Override
    public String getInternalCellValueAt(final int colIndex, final int rowIndex) {
        final XlsxCell cell = this.getCellAt(colIndex, rowIndex);
        return (cell != null && cell.value != null) ? cell.value : null;
    }

    @Override
    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(rowIndex);
        if (translatedRow == -1) {
            return 1;
        }
        return this.getMergeAcross(colIndex, translatedRow);
    }

    @Override
    public boolean isIgnorableRow(final int rowIndex) {
        if (rowIndex >= this.rows.size()) {
            return false;
        }

        final XlsxRow row = this.rows.get(rowIndex);
        if (row.isNotIgnorable) {
            return false;
        }

        final float height = row.height;

        int countEmptyCells = 0;
        int countCells = 0;
        boolean checkIfRowMergedVertically = false;
        if (row.cells != null) {
            for (final XlsxCell cell : row.cells) {
                if (cell.value == null) {
                    countEmptyCells++;
                }
                if (!checkIfRowMergedVertically && this.getMergeDown(countCells, rowIndex) > 0) {
                    checkIfRowMergedVertically = true;
                }
                countCells++;
            }
        }
        final float sparcity = (countCells == 0) ? 1.0f : (Float.valueOf(countEmptyCells) / Float.valueOf(countCells));

        boolean candidate = false;
        candidate |= (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
        candidate |= checkIfRowMergedVertically;
        candidate &= (sparcity >= DocumentFactory.DEFAULT_RATIO_SCARSITY);

        row.isNotIgnorable = !candidate;

        return candidate;
    }

    private XlsxCell getCellAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(rowIndex);
        if (translatedRow == -1 || translatedRow >= this.rows.size()) {
            return null;
        }

        final XlsxRow row = this.rows.get(translatedRow);
        if (row.cells == null || colIndex >= row.cells.size()) {
            return null;
        }

        return row.cells.get(colIndex);
    }

    private int getMergeAcross(final int colIndex, final int rowIndex) {
        if (mergedRegions.size() == 0) {
            return 1;
        }

        int numberOfCells = 1;
        for (final CellRangeAddress region : mergedRegions) {
            if (region.isInRange(rowIndex, colIndex)) {
                numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
                break;
            }
        }

        return numberOfCells;
    }

    private int getMergeDown(final int colIndex, final int rowIndex) {
        if (this.mergedRegions.size() == 0) {
            return 0;
        }

        int numberOfCells = 0;
        for (final CellRangeAddress region : mergedRegions) {
            if (region.isInRange(rowIndex, colIndex) && region.getLastRow() > region.getFirstRow()) {
                numberOfCells = region.getLastRow() - region.getFirstRow();
                break;
            }
        }

        return numberOfCells;
    }

    private final RowTranslator rowTranslator;
    private final String name;
    private final InputStream sheetData;
    private final StylesTable styles;
    private final SharedStringsTable sharedStrings;
    private final ArrayList<CellRangeAddress> mergedRegions = new ArrayList<CellRangeAddress>();
    private final ArrayList<XlsxRow> rows = new ArrayList<XlsxRow>();
}
