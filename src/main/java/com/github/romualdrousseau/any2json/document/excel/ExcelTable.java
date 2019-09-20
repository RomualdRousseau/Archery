package com.github.romualdrousseau.any2json.document.excel;

import com.github.romualdrousseau.any2json.Table;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;

class ExcelTable extends Table {
    public ExcelTable(Sheet sheet, FormulaEvaluator evaluator, int firstColumn, int firstRow, int lastColumn,
            int lastRow, int groupId) {
        this.sheet = sheet;
        this.evaluator = evaluator;
        this.formatter = new DataFormatter();
        buildDataTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    public ExcelTable(ExcelTable parent, int firstColumn, int firstRow, int lastColumn,
            int lastRow, int groupId) {
        this.sheet = parent.sheet;
        this.evaluator = parent.evaluator;
        this.formatter = parent.formatter;
        buildMetaTable(firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected ExcelRow getInternalRowAt(int i) {
        org.apache.poi.ss.usermodel.Row row = this.sheet.getRow(i);
        return (row != null) ? new ExcelRow(this, row, this.lastGroupId) : null;
    }

    protected ExcelTable createMetaTable(int firstColumn, int firstRow, int lastColumn, int lastRow, int groupId) {
        return new ExcelTable(this, firstColumn, firstRow, lastColumn, lastRow, groupId);
    }

    protected Sheet sheet;
    protected FormulaEvaluator evaluator;
    protected DataFormatter formatter;
}
