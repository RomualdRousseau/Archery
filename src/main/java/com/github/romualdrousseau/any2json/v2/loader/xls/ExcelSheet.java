package com.github.romualdrousseau.any2json.v2.loader.xls;

import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.intelli.IntelliSheet;
import com.github.romualdrousseau.shuju.util.StringUtility;

public class ExcelSheet extends IntelliSheet {

    public ExcelSheet(Sheet sheet) throws IOException {
        this.name = sheet.getSheetName();
        this.sheetDataFile = new ExcelMappedSheet().map(sheet);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getLastColumnNum(int rowIndex) {
        return  this.ensureRowsInMemory().get(rowIndex).lastColumnNum;
    }

    @Override
    public int getLastRowNum() {
        return  this.ensureRowsInMemory().size() - 1;
    }

    @Override
    public boolean hasCellDataAt(int colIndex, int rowIndex) {
        MappedCell cell = getCompressedCellAt(colIndex, rowIndex);
        return cell != null && cell.value != null;
    }

    @Override
    public String getInternalCellValueAt(int colIndex, int rowIndex) {
        MappedCell cell = getCompressedCellAt(colIndex, rowIndex);
        return (cell != null) ? StringUtility.cleanToken(cell.value) : null;
    }

    @Override
    public int getNumberOfMergedCellsAt(int colIndex, int rowIndex) {
        MappedCell cell = getCompressedCellAt(colIndex, rowIndex);
        return (cell != null) ? cell.length : 1;
    }

    private List<MappedRow> ensureRowsInMemory() {
        if (this.rows == null) {
            this.rows = new ExcelMappedSheet().unmap(this.sheetDataFile);
        }
        return this.rows;
    }

    private MappedCell getCompressedCellAt(int colIndex, int rowIndex) {
        MappedRow row = this.ensureRowsInMemory().get(rowIndex);
        int i = 0;
        MappedCell foundCell = null;
        for (MappedCell cell : row.cells) {
            if (i <= colIndex && colIndex < (i + cell.length)) {
                foundCell = (i == colIndex) ? cell : null;
                break;
            }
            i += cell.length;
        }
        return foundCell;
    }

    private String name;
    private File sheetDataFile;
    private List<MappedRow> rows;
}
