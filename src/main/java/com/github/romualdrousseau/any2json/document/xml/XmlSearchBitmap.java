package com.github.romualdrousseau.any2json.document.xml;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Worksheet;

public class XmlSearchBitmap extends ISearchBitmap {
    public XmlSearchBitmap(int columns, int rows) {
        this.width = columns;
        this.height = rows;
        this.data = new int[this.height][this.width];
    }

    public XmlSearchBitmap(Worksheet sheet, int columns, int rows) {
        this.width = columns;
        this.height = rows;
        this.data = new int[this.height][this.width];
        loadData(sheet);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int get(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return 0;
        }
        return this.data[y][x];
    }

    public void set(int x, int y, int v) {
        this.data[y][x] = v;
    }

    private void loadData(Worksheet sheet) {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                this.data[y][x] = getInternalCellValueAt(sheet, x, y);
            }
        }
    }

    private int getInternalCellValueAt(Worksheet sheet, int colIndex, int rowIndex) {
        Cell cell = sheet.getCellAt(rowIndex + 1, colIndex + 1);

        int firstColumn = checkIfMergedCell(sheet, cell, colIndex, rowIndex);
        if (firstColumn >= 0) {
            cell = sheet.getCellAt(rowIndex + 1, firstColumn + 1);
        }

        return (!cell.hasData()) ? 0 : 1;
    }

    private int checkIfMergedCell(Worksheet sheet, Cell cell, int colIndex, int rowIndex) {
        if (cell.hasData()) {
            return -1;
        }

        int firstColumn = colIndex;
        if(firstColumn > 0) {
            do {
                firstColumn--;
                cell = sheet.getCellAt(rowIndex + 1, firstColumn + 1);
            } while (firstColumn > 0 && !cell.hasData());
        }

        int lastColumn = firstColumn + cell.getMergeAcross();

        if (firstColumn <= colIndex && colIndex <= lastColumn) {
            return firstColumn;
        } else {
            return -1;
        }
    }

    private int width;
    private int height;
    private int[][] data;
}
