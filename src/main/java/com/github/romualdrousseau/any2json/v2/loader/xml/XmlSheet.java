package com.github.romualdrousseau.any2json.v2.loader.xml;

import com.github.romualdrousseau.shuju.util.StringUtility;

import nl.fountain.xelem.excel.Cell;
import nl.fountain.xelem.excel.Row;
import nl.fountain.xelem.excel.Worksheet;

import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;

class XmlSheet extends IntelliSheet {
    public XmlSheet(Worksheet sheet) {
        this.sheet = sheet;
    }

    @Override
    public String getName() {
        return this.sheet.getName();
    }

    @Override
    public int getLastColumnNum(int colIndex, int rowIndex) {
        Row row = this.sheet.getRowAt(rowIndex + 1);
        if (row == null) {
            return 0;
        }
        int colNum = colIndex;
        Cell cell = row.getCellAt(colNum + 1);
        while (cell.hasData() && !StringUtility.isEmpty(cell.getData$())) {
            cell = row.getCellAt((++colNum) + 1);
        }
        return colNum - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.sheet.getRows().size();
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        Cell cell = sheet.getCellAt(rowIndex + 1, colIndex + 1);
        if(cell == null || !cell.hasData()) {
            return null;
        }

        return StringUtility.cleanToken(cell.getData$());
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        Cell cell = sheet.getCellAt(rowIndex + 1, colIndex + 1);
		if(cell == null || !cell.hasData()) {
			return 1;
        }

		return cell.getMergeAcross() + 1;
    }

    private Worksheet sheet;
}
