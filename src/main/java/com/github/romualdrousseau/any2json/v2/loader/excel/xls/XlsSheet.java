package com.github.romualdrousseau.any2json.v2.loader.excel.xls;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class XlsSheet extends IntelliSheet implements RowTranslatable {

    public XlsSheet(Sheet sheet) throws IOException {
        this.sheet = sheet;
        this.rowTranslator = new RowTranslator(this);
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
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return 0;
        }
        return row.getLastCellNum();
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getLastRowNum() - this.rowTranslator.getIgnoredRowCount();
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        return cell != null;
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (cell == null) {
            return null;
        }
        return StringUtility.cleanToken(this.getData(cell));
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (cell == null) {
            return 1;
        }
        return this.getMergeAcross(cell) + 1;
    }

    @Override
    public boolean isIgnorableRow(int rowIndex) {
        if (rowIndex > this.sheet.getLastRowNum()) {
            return false;
        }

        Row row = this.sheet.getRow(rowIndex);
        if (row == null || rowIndex > this.sheet.getLastRowNum()) {
            return false;
        }

        int countEmptyCells = 0;
        int countCells = 0;
        boolean checkIfRowMergedVertically = false;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (!this.hasData(cell) || this.getData(cell).isEmpty()) {
                countEmptyCells++;
            }
            if (!checkIfRowMergedVertically && this.getMergeDown(cell) > 0) {
                checkIfRowMergedVertically = true;
            }
            countCells++;
        }

        double height = row.getHeight() * 0.07; // Rougly convert in pixels
        final float sparcity = (countCells == 0) ? 1.0f : (Float.valueOf(countEmptyCells) / Float.valueOf(countCells));

        boolean isIgnorable = false;
        isIgnorable |= (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
        isIgnorable |= checkIfRowMergedVertically;
        isIgnorable &= (sparcity >= DocumentFactory.DEFAULT_RATIO_SCARSITY);

        return isIgnorable;
    }

    private Row getRowAt(int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(rowIndex);
        if (translatedRow == -1) {
            return null;
        }
        return this.sheet.getRow(translatedRow);
    }

    private Cell getCellAt(int colIndex, int rowIndex) {
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        return this.hasData(cell) ? cell : null;
    }

    private boolean hasData(Cell cell) {
        if (cell == null) {
            return false;
        }

        final CellType type = cell.getCellType();

        if (!type.equals(CellType.BLANK) && !(type.equals(CellType.STRING) && cell.getStringCellValue().isEmpty())) {
            return true;
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

    private int getMergeAcross(Cell cell) {
        if (this.cachedRegion.size() == 0) {
            return 0;
        }
        if (cell == null) {
            return 0;
        }

        int numberOfCells = 0;
        for (CellRangeAddress region : this.cachedRegion) {
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                numberOfCells = region.getLastColumn() - region.getFirstColumn();
                break;
            }
        }

        return numberOfCells;
    }

    private int getMergeDown(Cell cell) {
        if (this.cachedRegion.size() == 0) {
            return 0;
        }
        if (cell == null) {
            return 0;
        }

        int numberOfCells = 0;
        for (final CellRangeAddress region : cachedRegion) {
            if (region.getLastRow() > region.getFirstRow()
                    && region.isInRange(cell.getRowIndex(), cell.getColumnIndex())
                    && cell.getRowIndex() > region.getFirstRow()) {
                numberOfCells = region.getLastRow() - region.getFirstRow();
                break;
            }
        }

        return numberOfCells;
    }

    private Sheet sheet;
    private RowTranslator rowTranslator;
    private ArrayList<CellRangeAddress> cachedRegion;
}
