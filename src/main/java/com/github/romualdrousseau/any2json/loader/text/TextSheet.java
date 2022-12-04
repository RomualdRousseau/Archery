package com.github.romualdrousseau.any2json.loader.text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.romualdrousseau.any2json.base.SheetBitmap;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.IntelliSheet;
import com.github.romualdrousseau.any2json.intelli.MetaTable;
import com.github.romualdrousseau.shuju.util.StringUtility;

class TextSheet extends IntelliSheet {

    public TextSheet(String name, List<String[]> rows) {
        this.name = name;
        this.rows = rows;
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

    @Override
    protected SheetBitmap getSheetBitmap() {
        return null;
    }

    @Override
    protected List<CompositeTable> findAllTables(final SheetBitmap image) {
        final LinkedList<CompositeTable> tables = new  LinkedList<CompositeTable>();
        tables.add(new CompositeTable(this, 0, 0, this.getLastColumnNum(), this.getLastRowNum()));
        return tables;
    }

    @Override
    protected List<DataTable> getDataTables(final List<CompositeTable> tables, final List<TableMatcher> dataMatchers) {
        LinkedList<DataTable> dataTables = new  LinkedList<DataTable>();
        dataTables.add(new DataTable(tables.get(0)));
        return dataTables;
    }

    @Override
    protected List<MetaTable> getMetaTables(final List<CompositeTable> tables, final List<TableMatcher> metaMatchers) {
        final ArrayList<MetaTable> result = new ArrayList<MetaTable>();
        return result;
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
