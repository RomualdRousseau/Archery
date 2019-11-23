package com.github.romualdrousseau.any2json.v2.loader.excel;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

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
            try {
                value = this.formatter.formatCellValue(cell, evaluator);
            } catch(NotImplementedException x) {
                value = "#ERROR?";
            }
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
    public boolean isIgnorableRow(int rowIndex) {
        if (rowIndex > (this.sheet.getLastRowNum() + 1)) {
            return false;
        }

        Row row = this.sheet.getRow(rowIndex);
        if (row == null || rowIndex > this.sheet.getLastRowNum()) {
            return false;
        }

        double height = row.getHeight() * 0.07; // Rougly convert in pixels

        float sparcity = Float.valueOf(row.getPhysicalNumberOfCells())
                / Float.valueOf(row.getLastCellNum() - row.getFirstCellNum());

        boolean candidate = false;
        candidate |= (height < DocumentFactory.SEPARATOR_ROW_THRESHOLD);
        candidate |= this.checkIfRowMergedVertically(row);
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
        return row.getCell(colIndex);
    }

    private boolean isCellBlank(Cell cell) {
        return (cell == null || (cell.getCellType() == Cell.CELL_TYPE_BLANK
                && cell.getCellStyle().getFillBackgroundColorColor() == null));
    }

    private boolean checkIfRowMergedVertically(Row row) {
        boolean result= false;

        if (this.cachedRegion.size()== 0) {
            return false;
        }

        Iterator<Cell> it = row.cellIterator();
        while (!result && it.hasNext()) {
            Cell cell = it.next();
            if(!isCellBlank(cell)) {
                continue;
            }

            for (CellRangeAddress region : this.cachedRegion) {
                if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                    result |= (region.getLastRow() > region.getFirstRow());
                }
            }
        }

        return result;
    }

    private org.apache.poi.ss.usermodel.Sheet sheet;
    private FormulaEvaluator evaluator;
    private DataFormatter formatter;
    private RowTranslator rowTranslator;
    private ArrayList<CellRangeAddress> cachedRegion;
}
