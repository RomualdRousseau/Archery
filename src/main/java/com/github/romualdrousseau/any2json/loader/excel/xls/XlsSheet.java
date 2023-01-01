package com.github.romualdrousseau.any2json.loader.excel.xls;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.shuju.util.StringUtils;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class XlsSheet implements SheetStore {

    public XlsSheet(Sheet sheet) throws IOException {
        this.sheet = sheet;
        this.cachedRegion = new ArrayList<CellRangeAddress>();
        for (int j = 0; j < this.sheet.getNumMergedRegions(); j++) {
            CellRangeAddress region = this.sheet.getMergedRegion(j);
            this.cachedRegion.add(region);
        }
    }

    @Override
    public String getName() {
        return this.sheet.getSheetName();
    }

    @Override
    public int getLastColumnNum(int rowIndex) {
        Row row = this.sheet.getRow(rowIndex);
        if (row == null) {
            return 0;
        }
        return row.getLastCellNum();
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getLastRowNum();
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        final int n = this.getInternalMergeDown(colIndex, rowIndex);
        final Row row = this.sheet.getRow(n);
        if (row == null) {
            return false;
        }
        final Cell cell = row.getCell(colIndex);
        return this.hasData(cell) || this.hasDecoration(cell);
    }

    @Override
    public boolean hasCellDecorationAt(int colIndex, int rowIndex) {
        final int n = this.getInternalMergeDown(colIndex, rowIndex);
        final Row row = this.sheet.getRow(n);
        if (row == null) {
            return false;
        }
        final Cell cell = row.getCell(colIndex);
        return this.hasDecoration(cell);
    }

    @Override
    public String getCellDataAt(int colIndex, int rowIndex) {
        final int n = this.getInternalMergeDown(colIndex, rowIndex);
        final Row row = this.sheet.getRow(n);
        if (row == null) {
            return null;
        }
        final Cell cell = row.getCell(colIndex);
        return this.hasData(cell) || this.hasDecoration(cell) ? StringUtils.cleanToken(this.getData(cell)) : null;
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        if (this.cachedRegion.size() == 0) {
            return 1;
        }

        int numberOfCells = 0;
        for (CellRangeAddress region : this.cachedRegion) {
            if (region.isInRange(rowIndex, colIndex)) {
                numberOfCells = region.getLastColumn() - region.getFirstColumn();
                break;
            }
        }

        return numberOfCells + 1;
    }

    @Override
    public void patchCell(int colIndex1, int rowIndex1, int colIndex2, int rowIndex2, final String value) {
        final Row row1 = this.sheet.getRow(rowIndex1);
        if (row1 == null) {
            return;
        }
        final Cell cell1 = row1.getCell(colIndex1);
        final Row row2 = this.sheet.getRow(rowIndex2);
        if (row2 == null) {
            return;
        }
        final Cell cell2 = row2.getCell(colIndex2);
        cell2.setCellStyle(cell1.getCellStyle());
        if(value == null) {
            cell2.setCellValue(this.getData(cell2));
        }
        else {
            cell2.setCellValue(value);
        }
    }

    private int getInternalMergeDown(int colIndex, int rowIndex) {
        if (this.cachedRegion.size() == 0) {
            return rowIndex;
        }

        int rowToReturn = rowIndex;
        for (final CellRangeAddress region : cachedRegion) {
            if (region.getLastRow() > region.getFirstRow() && rowIndex > region.getFirstRow()
                    && region.isInRange(rowIndex, colIndex)) {
                rowToReturn = region.getFirstRow();
                break;
            }
        }

        return rowToReturn;
    }

    private boolean hasData(Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellType type = cell.getCellType();

        if (!type.equals(CellType.BLANK) && !(type.equals(CellType.STRING) && cell.getStringCellValue().isEmpty())) {
            return true;
        }

        return false;
    }

    private boolean hasDecoration(Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellStyle style = cell.getCellStyle();

        // Keep cell with colored borders
        if (!style.getBorderLeft().equals(BorderStyle.NONE) && !style.getBorderRight().equals(BorderStyle.NONE)
                && !style.getBorderTop().equals(BorderStyle.NONE)
                && !style.getBorderBottom().equals(BorderStyle.NONE)) {
            return true;
        }

        // Keep cell with a colored (not automatic and not white) pattern
        final Color bkcolor = style.getFillBackgroundColorColor();
        if (bkcolor != null) {
            if (bkcolor instanceof XSSFColor) {
                if (((XSSFColor) bkcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                        && (((XSSFColor) bkcolor).getARGBHex() == null
                                || !((XSSFColor) bkcolor).getARGBHex().equals("FFFFFFFF"))) {
                    return true;
                }
            }
            if (bkcolor instanceof HSSFColor) {
                if (((HSSFColor) bkcolor).getIndex() != HSSFColor.HSSFColorPredefined.AUTOMATIC.getIndex()
                        && (((HSSFColor) bkcolor).getHexString() == null
                                || !((HSSFColor) bkcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                    return true;
                }
            }
        }

        // Keep cell with a colored (not automatic and not white) background
        final Color fgcolor = style.getFillForegroundColorColor();
        if (fgcolor != null) {
            if (fgcolor instanceof XSSFColor) {
                if (((XSSFColor) fgcolor).getIndexed() != IndexedColors.AUTOMATIC.index
                        && (((XSSFColor) fgcolor).getARGBHex() == null
                                || !((XSSFColor) fgcolor).getARGBHex().equals("FFFFFFFF"))) {
                    return true;
                }
            }
            if (fgcolor instanceof HSSFColor) {
                if (((HSSFColor) fgcolor).getIndex() != HSSFColor.HSSFColorPredefined.AUTOMATIC.getIndex()
                        && (((HSSFColor) fgcolor).getHexString() != null
                                || !((HSSFColor) fgcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getData(Cell cell) {
        CellType type = cell.getCellType();
        if (type.equals(CellType.FORMULA)) {
            type = cell.getCachedFormulaResultType();
        }

        String value = "";

        switch (cell.getCellType()) {
            case STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    double d = cell.getNumericCellValue();
                    if (d != Math.rint(d)) {
                        value = String.valueOf(cell.getNumericCellValue());
                    } else {
                        value = String.valueOf((int) cell.getNumericCellValue());
                    }
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue() ? "TRUE" : "FALSE";
                break;
            default:
                // Do nothing
        }

        return value;
    }

    private Sheet sheet;
    private ArrayList<CellRangeAddress> cachedRegion;
}
