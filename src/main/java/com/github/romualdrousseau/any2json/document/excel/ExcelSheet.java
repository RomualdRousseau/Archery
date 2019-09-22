package com.github.romualdrousseau.any2json.document.excel;

import java.util.List;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;
import com.github.romualdrousseau.shuju.util.StringUtility;
import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.Sheet;

class ExcelSheet extends Sheet {
    public ExcelSheet(org.apache.poi.ss.usermodel.Sheet sheet, FormulaEvaluator evaluator) {
        this.sheet = sheet;
        this.table = null;
        this.evaluator = evaluator;
    }

    public String getName() {
        return this.sheet.getSheetName();
    }

    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows) {
        return new ExcelSearchBitmap(this.sheet, headerColumns, headerRows);
    }

    public ITable getTable() {
        int lastColumnNum = this.estimateLastColumnNum(0, 0);
        if (this.table == null && lastColumnNum > 0) {
            this.table = new ExcelTable(this.sheet, this.evaluator, 0, 0, lastColumnNum, this.sheet.getLastRowNum(), 0);
        }
        return this.table;
    }

    public ITable findTable(int headerColumns, int headerRows) {
        final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));

        if (this.table == null) {
            ExcelSearchBitmap bitmap = new ExcelSearchBitmap(this.sheet, headerColumns, headerRows);
            filter.apply(bitmap, 1);
            filter.applyNeg(bitmap, 2);

            SearchPoint[] table = new RectangleExtractor().extractBest(bitmap);
            // debug(bitmap1, table);
            if (table != null && table[1].getX() > table[0].getX()) {
                int lastColumnNum = Math.max(table[1].getX(),
                        this.estimateLastColumnNum(table[0].getX(), table[0].getY()));
                this.table = new ExcelTable(this.sheet, this.evaluator, table[0].getX(), table[0].getY(), lastColumnNum,
                        this.sheet.getLastRowNum(), 0);
            }
        }
        return this.table;
    }

    public List<ITable> findTables(int headerColumns, int headerRows) {
        final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));

        ArrayList<ITable> result = new ArrayList<ITable>();

        ExcelSearchBitmap bitmap = new ExcelSearchBitmap(this.sheet, headerColumns, headerRows);
        filter.apply(bitmap, 1);
        filter.applyNeg(bitmap, 2);

        List<SearchPoint[]> tables = new RectangleExtractor().extractAll(bitmap);
        for (SearchPoint[] table : tables) {
            if (table[1].getX() > table[0].getX()) {
                int lastColumnNum = Math.max(table[1].getX(),
                        this.estimateLastColumnNum(table[0].getX(), table[0].getY()));
                int lastRowNum = ((table[1].getY() + 1) < headerRows) ? table[1].getY() : this.sheet.getLastRowNum();
                result.add(new ExcelTable(this.sheet, this.evaluator, table[0].getX(), table[0].getY(), lastColumnNum,
                        lastRowNum, 0));
            }
        }
        return result;
    }

    private int estimateLastColumnNum(int x, int y) {
        Row row = this.sheet.getRow(y);
        if (row == null) {
            return 0;
        }
        int colNum = x;
        DataFormatter formatter = new DataFormatter();
        Cell cell = row.getCell(colNum);
        while (cell != null && !StringUtility.isEmpty(formatter.formatCellValue(cell))) {
            cell = row.getCell(++colNum);
        }
        return colNum - 1;
    }

    // private void debug(ExcelSearchBitmap searchBitmap, SearchPoint[] table) {
    // for (int i = 0; i < searchBitmap.getHeight(); i++) {
    // for (int j = 0; j < searchBitmap.getWidth(); j++) {
    // if (searchBitmap.get(j, i) == 0) {
    // System.out.print("_");
    // } else {
    // System.out.print("#");
    // }
    // }
    // System.out.println();
    // }
    // if (table != null) {
    // System.out.print(table[0].getX() + " ");
    // System.out.print(table[0].getY() + " ");
    // System.out.print(table[1].getX() + " ");
    // System.out.println(table[1].getY());
    // }
    // }

    private org.apache.poi.ss.usermodel.Sheet sheet;
    private ExcelTable table;
    private FormulaEvaluator evaluator;
}
