package com.github.romualdrousseau.any2json.v2.loader.excel;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;
import com.github.romualdrousseau.shuju.util.StringUtility;

class ExcelSheet extends IntelliSheet implements RowTranslatable {

    public ExcelSheet(org.apache.poi.ss.usermodel.Sheet sheet, FormulaEvaluator evaluator) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
        this.rowTranslator = new RowTranslator(this);

        this.evaluator.setIgnoreMissingWorkbooks(true);

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
        return this.sheet.getLastRowNum() + 1;
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

        int type = cell.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        String value = "";

        if (type == Cell.CELL_TYPE_BOOLEAN) {
            value = cell.getBooleanCellValue() ? "TRUE" : "FALSE";
        } else if (type == Cell.CELL_TYPE_STRING) {
            value = cell.getStringCellValue();
        } else if (type == Cell.CELL_TYPE_NUMERIC) {
            try {
                value = this.formatter.formatCellValue(cell, evaluator);
                if (value.matches("-?\\d+")) {
                    double d = cell.getNumericCellValue();
                    if (d != Math.rint(d)) {
                        value = String.valueOf(d);
                    }
                }
            } catch (NotImplementedException x) {
            }
        }

        return StringUtility.cleanToken(value);
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        if (this.cachedRegion.size() == 0) {
            return 1;
        }

        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (cell == null) {
            return 1;
        }

        int numberOfCells = 1;
        for (CellRangeAddress region : this.cachedRegion) {
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
                break;
            }
        }

        return numberOfCells;
    }

    @Override
    public boolean isIgnorableRow(int rowIndex) {
        if (rowIndex > (this.sheet.getLastRowNum() + 1)) {
            return false;
        }

        Row row = this.sheet.getRow(rowIndex);
        if (row == null || rowIndex > this.sheet.getLastRowNum()) {
            return false;
        }

        double height = row.getHeight() * 0.07; // Rougly convert in pixels

        int merged = countOfRowMergedVertically(row);

        float sparcity = Float.valueOf(merged)
                / Float.valueOf(row.getLastCellNum() - row.getFirstCellNum());

        boolean candidate = false;
        candidate |= (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
        candidate |= (merged > 0);
        candidate &= (sparcity >= DocumentFactory.DEFAULT_RATIO_SCARSITY);
        return candidate;
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
        if (!hasData(cell)) {
            return null;
        }

        return cell;
    }

    private int countOfRowMergedVertically(Row row) {
        int result = 0;

        if (this.cachedRegion.size() == 0) {
            return 0;
        }

        Iterator<Cell> it = row.cellIterator();
        while (it.hasNext()) {
            Cell cell = it.next();
            for (CellRangeAddress region : this.cachedRegion) {
                if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                    if((cell.getRowIndex() > region.getFirstRow())
                            && (region.getLastRow() > region.getFirstRow())) {
                        result++;
                    }
                }
            }
        }

        return result;
    }

    private boolean hasData(Cell cell) {
        if (cell == null) {
            return false;
        }

        final int type = cell.getCellType();

        if (type == Cell.CELL_TYPE_BLANK || type == Cell.CELL_TYPE_STRING && cell.getStringCellValue().isEmpty()) {
            final CellStyle style = cell.getCellStyle();

            // Keep cell with colored borders
            if (style.getBorderLeft() != CellStyle.BORDER_NONE && style.getBorderRight() != CellStyle.BORDER_NONE
                    && style.getBorderTop() != CellStyle.BORDER_NONE
                    && style.getBorderBottom() != CellStyle.BORDER_NONE) {
                // if (style.getLeftBorderColor() != 0 && style.getRightBorderColor() != 0
                //         && style.getTopBorderColor() != 0 && style.getBottomBorderColor() != 0) {
                //     return true;
                // }
                return true;
            }

            // Keep cell with a colored (not automatic and not white) pattern
            final Color bkcolor = style.getFillBackgroundColorColor();
            if (bkcolor != null) {
                if(bkcolor instanceof XSSFColor) {
                    if(((XSSFColor) bkcolor).getIndexed() != IndexedColors.AUTOMATIC.index && (((XSSFColor) bkcolor).getARGBHex() == null || !((XSSFColor) bkcolor).getARGBHex().equals("FFFFFFFF"))) {
                        return true;
                    }
                }
                if(bkcolor instanceof HSSFColor) {
                    if(((HSSFColor) bkcolor).getIndex() != HSSFColor.AUTOMATIC.index && (((HSSFColor) bkcolor).getHexString() == null || !((HSSFColor) bkcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                        return true;
                    }
                }
            }

            // Keep cell with a colored (not automatic and not white) background
            final Color fgcolor = style.getFillForegroundColorColor();
            if (fgcolor != null) {
                if(fgcolor instanceof XSSFColor) {
                    if(((XSSFColor) fgcolor).getIndexed() != IndexedColors.AUTOMATIC.index && (((XSSFColor) fgcolor).getARGBHex() == null || !((XSSFColor) fgcolor).getARGBHex().equals("FFFFFFFF"))) {
                        return true;
                    }
                }
                if(fgcolor instanceof HSSFColor) {
                    if(((HSSFColor) fgcolor).getIndex() != HSSFColor.AUTOMATIC.index && (((HSSFColor) fgcolor).getHexString() != null || !((HSSFColor) fgcolor).getHexString().equals("FFFF:FFFF:FFFF"))) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            return true;
        }
    }

    private org.apache.poi.ss.usermodel.Sheet sheet;
    private FormulaEvaluator evaluator;
    private DataFormatter formatter;
    private RowTranslator rowTranslator;
    private ArrayList<CellRangeAddress> cachedRegion;
}
