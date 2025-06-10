package com.github.romualdrousseau.archery.loader.excel.xlsx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.romualdrousseau.archery.commons.collections.DataFrame;
import com.github.romualdrousseau.archery.commons.collections.DataFrameWriter;
import com.github.romualdrousseau.archery.commons.collections.Row;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class ContentHandler extends DefaultHandler {

    private class Cell {
        CellAddress address;
        CellType type;
        CellStyle style;
        String value;
        boolean decorated;
    }

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private final DataFrameWriter rows;
    private final StylesTable styles;
    private final SharedStrings sharedStrings;
    private final List<CellRangeAddress> mergedRegions = new ArrayList<>();

    private ArrayList<String> row;
    private boolean startValue;
    private boolean inlineStr;
    private Cell currCell;
    private Cell prevCell;

    public ContentHandler(final DataFrameWriter dataFrameWriter, final SharedStrings sharedStrings,
            final StylesTable styles) {
        this.rows = dataFrameWriter;
        this.styles = styles;
        this.sharedStrings = sharedStrings;
    }

    public List<CellRangeAddress> getMergesRegions() {
        return this.mergedRegions;
    }

    public DataFrame getRows() throws IOException {
        return this.rows.getDataFrame();
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes attributes) {
        if ("row".equals(name)) {
            if (attributes.getValue("r") != null) {
                if (attributes.getValue("r") != null) {
                    this.fillMissingRows(Integer.valueOf(attributes.getValue("r")) - 1);
                }
                this.row = new ArrayList<String>();
                this.prevCell = null;
                this.currCell = null;
            }
        } else if ("c".equals(name)) {
            this.prevCell = this.currCell;
            this.currCell = new Cell();
            this.currCell.address = (attributes.getValue("r") != null)
                    ? new CellAddress(attributes.getValue("r"))
                    : CellAddress.A1;
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
            final var mergedRegion = CellRangeAddress.valueOf(attributes.getValue("ref"));
            if (mergedRegion.getNumberOfCells() > 1) {
                mergedRegions.add(mergedRegion);
            }
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
                    this.row.add(StringUtils.cleanToken(this.currCell.value));
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
}
