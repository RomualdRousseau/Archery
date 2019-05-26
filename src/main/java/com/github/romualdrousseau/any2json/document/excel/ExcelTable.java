package com.github.romualdrousseau.any2json.document.excel;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.TableRow;
import com.github.romualdrousseau.shuju.util.StringUtility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

class ExcelTable extends Table {
    public ExcelTable(Sheet sheet, FormulaEvaluator evaluator, int firstColumn, int firstRow, int lastColumn,
            int lastRow) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
        this.firstColumn = firstColumn;
        this.firstRow = firstRow + 1;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;

        skipEmptyFirstRows(0.5);

        processHeaders();
    }

    public int getNumberOfColumns() {
        return this.lastColumn - this.firstColumn + 1;
    }

    public int getNumberOfRows() {
        return this.lastRow - this.firstRow + 1;
    }

    public TableRow getRowAt(int i) {
        if (i < 0 || i >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(i);
        }

        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(this.firstRow + i);
        return (row != null) ? new ExcelRow(this, row) : null;
    }

    private void processHeaders() {
        int ignoreCells = 0;
        for (Cell cell : this.sheet.getRow(this.firstRow - 1)) {
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
            addHeader(header);

            ignoreCells = header.getNumberOfCells() - 1;
        }
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

    private void skipEmptyFirstRows(double ratioOfEmptiness) {
        for (int i = 0; i < Math.min(10, getNumberOfRows()); i++) {
            org.apache.poi.ss.usermodel.Row tmp = this.sheet.getRow(this.firstRow - 1);
            TableRow row = (tmp != null) ? new ExcelRow(this, tmp) : null;

            if (row != null) {
                double emptinessFirstCell = Double.valueOf(row.getNumberOfMergedCellsAt(0))
                        / Double.valueOf(row.getNumberOfCells());
                if (emptinessFirstCell < 0.5 && !row.isEmpty(0.5)) {
                    break;
                }
            }

            this.firstRow++;
        }
    }

    protected Sheet sheet;
    protected FormulaEvaluator evaluator;
    protected DataFormatter formatter;
    protected int firstColumn;
    protected int firstRow;
    protected int lastColumn;
    protected int lastRow;
}
