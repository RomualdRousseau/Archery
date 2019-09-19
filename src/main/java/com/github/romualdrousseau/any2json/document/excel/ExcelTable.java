package com.github.romualdrousseau.any2json.document.excel;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.shuju.util.StringUtility;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

class ExcelTable extends Table {
    public ExcelTable(Sheet sheet, FormulaEvaluator evaluator, int firstColumn, int firstRow, int lastColumn,
            int lastRow, int groupId) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
        buildTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected ExcelRow getInternalRowAt(int i) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(i);
        return (row != null) ? new ExcelRow(this, row) : null;
    }

    protected ExcelTable createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        return new ExcelTable(this.sheet, this.evaluator, firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected List<TableHeader> getHeadersAt(int i) {
        ArrayList<TableHeader> result = new ArrayList<TableHeader>();

        Row cells = this.sheet.getRow(i);
        if (cells == null) {
            return result;
        }

        int ignoreCells = 0;
        for (Cell cell : cells) {
            if (cell.getColumnIndex() < this.firstColumn || cell.getColumnIndex() > this.lastColumn) {
                continue;
            }

            if (ignoreCells > 0) {
                ignoreCells--;
                continue;
            }

            TableHeader header = new TableHeader().setColumnIndex(cell.getColumnIndex() - this.firstColumn)
                    .setNumberOfCells(findNumberOfCells(cell))
                    .setName(StringUtility.cleanToken(this.formatter.formatCellValue(cell))).setTag(null);

            ignoreCells = header.getNumberOfCells() - 1;

            result.add(header);
        }
        return result;
    }

    private int findNumberOfCells(Cell cell) {
        int numberOfCells = 1;
        for (int i = 0; i < this.sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = this.sheet.getMergedRegion(i);
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                numberOfCells = (region.getLastColumn() - region.getFirstColumn()) + 1;
            }
        }
        return numberOfCells;
    }

    protected Sheet sheet;
    protected FormulaEvaluator evaluator;
    protected DataFormatter formatter;
}
