package com.github.romualdrousseau.any2json.loader.text;

import java.util.List;

import com.github.romualdrousseau.any2json.simple.SimpleSheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

class TextSheet extends SimpleSheet {

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
        String cell = this.getCellAt(colIndex, rowIndex);
        return cell != null && !cell.isEmpty();
    }

    @Override
    public boolean hasCellDecorationAt(int colIndex, int rowIndex) {
        return false;
    }

    @Override
    public String getCellDataAt(int colIndex, int rowIndex) {
        String cell = this.getCellAt(colIndex, rowIndex);
        if(cell == null || cell.isEmpty()) {
            return null;
        }
        return StringUtility.cleanToken(cell);
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        return 1;
    }

    private String getCellAt(int colIndex, int rowIndex) {
        if(rowIndex >= this.rows.size()) {
            return null;
        }

        String[] row = this.rows.get(rowIndex);

        if(colIndex >= row.length) {
            return null;
        }

        return row[colIndex];
    }

    private String name;
    private List<String[]> rows;
}
