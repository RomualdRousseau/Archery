package com.github.romualdrousseau.any2json.loader.excel.xml;

import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.shuju.util.StringUtils;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

class XmlSheet implements SheetStore {

    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(int rowIndex) {
        Row row = this.sheet.getRowAt(rowIndex + 1);
        return row.maxCellIndex() - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows().size() - 1;
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        final int n = this.getInternalMergeDown(colIndex, rowIndex);
        Cell cell = this.sheet.getCellAt(n + 1, colIndex + 1);
        return cell.hasData();
    }

    @Override
    public boolean hasCellDecorationAt(int colIndex, int rowIndex) {
        return false;
    }

    @Override
    public String getCellDataAt(int colIndex, int rowIndex) {
        final int n = this.getInternalMergeDown(colIndex, rowIndex);
        Cell cell = this.sheet.getCellAt(n + 1, colIndex + 1);
        return cell.hasData() ? StringUtils.cleanToken(cell.getData$()) : null;
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Cell cell = this.sheet.getCellAt(rowIndex + 1, colIndex + 1);
        return cell.getMergeAcross() + 1;
    }

    @Override
    public void patchCell(int colIndex1, int rowIndex1, int colIndex2, int rowIndex2, final String value, final boolean unmergeAll) {
        final int n1 = this.getInternalMergeDown(colIndex1, rowIndex1);
        final int n2 = this.getInternalMergeDown(colIndex2, rowIndex2);
        if(value == null) {
            this.sheet.addCellAt(n2 + 1, colIndex2 + 1, this.sheet.getCellAt(n1 + 1, colIndex1 + 1));
        }
        else {
            this.sheet.addCellAt(n2 + 1, colIndex2 + 1).setData(value);
        }
    }

    private int getInternalMergeDown(int colIndex, int rowIndex) {
        if (rowIndex <= 0) {
            return 0;
        }

        int rowToReturn = rowIndex;
        for (int i = 1; i < 5; i++) {
            int firstRow = rowIndex - i;
            if (firstRow < 0) {
                break;
            }

            int lastRow = firstRow + this.sheet.getCellAt(firstRow + 1, colIndex + 1).getMergeDown();
            if (lastRow > firstRow && firstRow <= rowIndex && rowIndex <= lastRow) {
                rowToReturn = firstRow;
                break;
            }
        }

        return rowToReturn;
    }

    private Worksheet sheet;
}
