package com.github.romualdrousseau.any2json.loader.excel.xml;

import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

class XmlSheet extends IntelliSheet {

    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public String getName() {
        return this.sheet.getName();
    }

    @Override
    protected int getInternalLastColumnNum(int rowIndex) {
        Row row = this.sheet.getRowAt(rowIndex + 1);
        return row.maxCellIndex();
    }

    @Override
    protected int getInternalLastRowNum() {
        return this.sheet.getRows().size() - 1;
    }

    @Override
    protected boolean hasInternalCellDataAt(int colIndex, int rowIndex) {
        Cell cell = this.sheet.getCellAt(rowIndex + 1, colIndex + 1);
        return cell.hasData();
    }

    @Override
    protected String getInternalCellDataAt(int colIndex, int rowIndex) {
        Cell cell = this.sheet.getCellAt(rowIndex + 1, colIndex + 1);
        return cell.hasData() ? StringUtility.cleanToken(cell.getData$()) : null;
    }

    @Override
    protected int getInternalMergeAcross(int colIndex, int rowIndex) {
        Cell cell = this.sheet.getCellAt(rowIndex + 1, colIndex + 1);
        return cell.getMergeAcross() + 1;
    }

    @Override
    protected int getInternalMergeDown(int colIndex, int rowIndex) {
        if (rowIndex <= 0) {
            return 0;
        }

        int numberOfCells = 0;
        for (int i = 1; i < 5; i++) {
            int firstRow = rowIndex - i;
            if (firstRow < 0) {
                break;
            }

            int lastRow = firstRow + this.sheet.getCellAt(firstRow + 1, colIndex + 1).getMergeDown();
            if (lastRow > firstRow && firstRow <= rowIndex && rowIndex <= lastRow) {
                numberOfCells = lastRow - firstRow;
                break;
            }
        }

        return numberOfCells;
    }

    private Worksheet sheet;
}
