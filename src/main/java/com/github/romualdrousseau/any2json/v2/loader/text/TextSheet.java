package com.github.romualdrousseau.any2json.v2.loader.text;

import java.util.List;

import com.github.romualdrousseau.any2json.v2.base.AbstractSheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

class TextSheet extends AbstractSheet {

    public TextSheet(String name, List<String[]> rows) {
        this.name = name;
        this.rows =  rows;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(int rowIndex) {
        return this.rows.get(rowIndex).length - 1;
    }

    @Override
    public int getLastRowNum() {
        return this.rows.size() - 1;
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        String cell = this.rows.get(rowIndex)[colIndex];
        return cell != null && !cell.isEmpty();
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        String cell = this.rows.get(rowIndex)[colIndex];
        if(cell == null || cell.isEmpty()) {
            return null;
        }
        return StringUtility.cleanToken(cell);
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        return 1;
    }

    private String name;
    private List<String[]> rows;
}
