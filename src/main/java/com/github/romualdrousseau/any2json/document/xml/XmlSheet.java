package com.github.romualdrousseau.any2json.document.xml;

import java.util.List;
import java.util.ArrayList;

import com.github.romualdrousseau.shuju.cv.Filter;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;
import com.github.romualdrousseau.shuju.cv.Template;
import com.github.romualdrousseau.shuju.cv.SearchPoint;
import com.github.romualdrousseau.shuju.cv.templatematching.shapeextractor.RectangleExtractor;
import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

import com.github.romualdrousseau.any2json.ITable;
import com.github.romualdrousseau.any2json.Sheet;

class XmlSheet extends Sheet {
    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
        this.table = null;
    }

    public String getName() {
        return this.sheet.getName();
    }

    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows) {
        return new XmlSearchBitmap(this.sheet, headerColumns, headerRows);
    }

    public ITable getTable() {
        int lastColumnNum = this.estimateLastColumnNum(0, 0);
        if (this.table == null && lastColumnNum > 0) {
            this.table = new XmlTable(this.sheet, 0, 0, lastColumnNum, this.sheet.getRows().size(), 0);
            this.table.enableMetaTable(false);
            this.table.enableIntelliTable(false);
        }
        return this.table;
    }

    public ITable findTable(int headerColumns, int headerRows) {
        final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));

        if (this.table == null) {
            XmlSearchBitmap bitmap = new XmlSearchBitmap(this.sheet, headerColumns, headerRows);
            filter.apply(bitmap, 1);
            filter.applyNeg(bitmap, 2);

            SearchPoint[] table = new RectangleExtractor().extractBest(bitmap);
            // debug(bitmap1, table);
            if (table != null && table[1].getX() > table[0].getX()) {
                int lastColumnNum = Math.max(table[1].getX(),
                        this.estimateLastColumnNum(table[0].getX(), table[0].getY()));
                this.table = new XmlTable(this.sheet, table[0].getX(), table[0].getY(), lastColumnNum,
                        this.sheet.getRows().size(), 0);
            }
        }
        return this.table;
    }

    public List<ITable> findTables(int headerColumns, int headerRows) {
        final Filter filter = new Filter(new Template(new int[][] { { 0, 0, 0 }, { 1, 0, 1 }, { 0, 0, 0 } }));

        ArrayList<ITable> result = new ArrayList<ITable>();

        XmlSearchBitmap bitmap = new XmlSearchBitmap(this.sheet, headerColumns, headerRows);
        filter.apply(bitmap, 1);
        filter.applyNeg(bitmap, 2);

        List<SearchPoint[]> tables = new RectangleExtractor().extractAll(bitmap);
        for (SearchPoint[] table : tables) {
            if (table[1].getX() > table[0].getX()) {
                int lastColumnNum = Math.max(table[1].getX(),
                        this.estimateLastColumnNum(table[0].getX(), table[0].getY()));
                int lastRowNum = ((table[1].getY() + 1) < headerRows) ? table[1].getY() : this.sheet.getRows().size();
                result.add(new XmlTable(this.sheet, table[0].getX(), table[0].getY(), lastColumnNum, lastRowNum, 0));
            }
        }
        return result;
    }

    private int estimateLastColumnNum(int x, int y) {
        Row row = this.sheet.getRowAt(y + 1);
        if (row == null) {
            return 0;
        }
        int colNum = x;
        Cell cell = row.getCellAt(colNum + 1);
        while (cell.hasData() && !StringUtility.isEmpty(cell.getData$())) {
            cell = row.getCellAt((++colNum) + 1);
        }
        return colNum - 1;
    }

    private Worksheet sheet;
    private XmlTable table;
}
