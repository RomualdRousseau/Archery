package com.github.romualdrousseau.any2json.v2.loader.excel;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.v2.util.RowTranslatable;
import com.github.romualdrousseau.any2json.v2.util.RowTranslator;
import com.github.romualdrousseau.shuju.util.StringUtility;

class ExcelSheet extends IntelliSheet implements RowTranslatable {

    public ExcelSheet(org.apache.poi.ss.usermodel.Sheet sheet, FormulaEvaluator evaluator) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
        this.rowTranslator = new RowTranslator(this, getLastRowNum() + 1);

        this.evaluator.setIgnoreMissingWorkbooks(true);

        this.cachedRegion = new ArrayList<CellRangeAddress>();
        for (int j = 0; j < this.sheet.getNumMergedRegions(); j++) {
            CellRangeAddress region = this.sheet.getMergedRegion(j);
            this.cachedRegion.add(region);
        }

        this.lastCachedRowIndex = -1;
        this.lastCachedRow = null;
    }

    @Override
    public String getName() {
        return this.sheet.getSheetName();
    }

    @Override
    public int getLastColumnNum(int colIndex, int rowIndex) {
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return 0;
        }

        int colNum = colIndex;
        Cell cell = row.getCell(colNum);
        while (!isCellBlank(cell)) {
            // while (!isCellBlank(cell) &&
            // !StringUtility.isEmpty(this.formatter.formatCellValue(cell))) {
            cell = row.getCell(++colNum);
        }

        return colNum - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getLastRowNum() + 1;
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Cell cell = this.getCellAt(colIndex, rowIndex);
        if (this.isCellBlank(cell)) {
            return null;
        }

        int type = cell.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        String value = "";

        if (type == Cell.CELL_TYPE_ERROR) {
            value = "#ERROR?";
        } else if (type == Cell.CELL_TYPE_BOOLEAN) {
            value = cell.getBooleanCellValue() ? "TRUE" : "FALSE";
        } else if (type == Cell.CELL_TYPE_STRING) {
            value = cell.getStringCellValue();
        } else if (type == Cell.CELL_TYPE_NUMERIC) {
            value = this.formatter.formatCellValue(cell, evaluator);
            if (value.matches("-?\\d+")) {
                double d = cell.getNumericCellValue();
                if (d != Math.rint(d)) {
                    value = String.valueOf(d);
                }
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
    public boolean isTranslatableRow(int colIndex, int rowIndex) {
        Row row = this.getCachedRowAt(rowIndex);
        if (row == null) {
            return false;
        }
        double height = row.getHeight() * 0.07; // Rougly convert in pixels
        return (height < IntelliSheet.SEPARATOR_ROW_THRESHOLD);
    }

    private Row getRowAt(int rowIndex) {
        final int translatedRow = this.rowTranslator.rebase(0, rowIndex);
        if (translatedRow == -1) {
            return null;
        }
        return this.getCachedRowAt(translatedRow);
    }

    private Cell getCellAt(int colIndex, int rowIndex) {
        Row row = this.getRowAt(rowIndex);
        if (row == null) {
            return null;
        }
        return this.lastCachedRow.getCell(colIndex);
    }

    private boolean isCellBlank(Cell cell) {
        return (cell == null || (cell.getCellType() == Cell.CELL_TYPE_BLANK
                && cell.getCellStyle().getFillBackgroundColorColor() == null));
    }

    private Row getCachedRowAt(int rowIndex) {
        if (rowIndex != this.lastCachedRowIndex) {
            this.lastCachedRow = this.sheet.getRow(rowIndex);
            this.lastCachedRowIndex = rowIndex;
        }
        return this.lastCachedRow;
    }

    private org.apache.poi.ss.usermodel.Sheet sheet;
    private FormulaEvaluator evaluator;
    private DataFormatter formatter;
    private RowTranslator rowTranslator;
    private ArrayList<CellRangeAddress> cachedRegion;
    private int lastCachedRowIndex;
    private Row lastCachedRow;
}
