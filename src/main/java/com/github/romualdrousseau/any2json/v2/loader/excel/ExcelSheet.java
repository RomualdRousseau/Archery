package com.github.romualdrousseau.any2json.v2.loader.excel;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

class ExcelSheet extends IntelliSheet {
    public ExcelSheet(org.apache.poi.ss.usermodel.Sheet sheet, FormulaEvaluator evaluator) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
    }

    @Override
    public String getName() {
        return this.sheet.getSheetName();
    }

    @Override
    public int getLastColumnNum(int colIndex, int rowIndex) {
        Row row = this.sheet.getRow(rowIndex);
        if (row == null) {
            return 0;
        }
        int colNum = colIndex;
        Cell cell = row.getCell(colNum);
        while (cell != null && !StringUtility.isEmpty(this.formatter.formatCellValue(cell))) {
            cell = row.getCell(++colNum);
        }
        return colNum - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getLastRowNum();
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Row row = this.sheet.getRow(rowIndex);
        if(row == null) {
            return null;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null || (cell.getCellType() == Cell.CELL_TYPE_BLANK
                && cell.getCellStyle().getFillBackgroundColorColor() == null)) {
            return null;
        }

        int type = Cell.CELL_TYPE_ERROR;
        String value = "#ERROR!";
        try {
            type = this.evaluator.evaluateInCell(cell).getCellType();
            value = this.formatter.formatCellValue(cell);
        } catch (Exception x) {
            type = Cell.CELL_TYPE_ERROR;
            value = "#ERROR!";
        }

        if (type == Cell.CELL_TYPE_NUMERIC && value.matches("-?\\d+")) {
            // TRICKY: Get hidden decimals in case of a rounded numeric value
            double d = cell.getNumericCellValue();
            value = (Math.floor(d) == d) ? value : String.valueOf(d);
        }

        return StringUtility.cleanToken(value);
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Row row = this.sheet.getRow(rowIndex);
        if(row == null) {
            return 1;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return 1;
        }

        int numberOfCells = 1;
        for (int j = 0; j < this.sheet.getNumMergedRegions(); j++) {
            CellRangeAddress region = this.sheet.getMergedRegion(j);
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
                break;
            }
        }

        return numberOfCells;
    }

    private org.apache.poi.ss.usermodel.Sheet sheet;
    private FormulaEvaluator evaluator;
    private DataFormatter formatter;
}
