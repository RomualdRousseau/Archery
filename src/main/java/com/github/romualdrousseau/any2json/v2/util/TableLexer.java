package com.github.romualdrousseau.any2json.v2.util;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.Table;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;
import com.github.romualdrousseau.any2json.v2.layex.Lexer;

public class TableLexer implements Lexer<BaseCell, TableLexer.Cursor> {

    class Cursor {

        public Cursor(final int colIndex, final int rowIndex) {
            this.colIndex = colIndex;
            this.rowIndex = rowIndex;
        }

        public int getColIndex() {
            return this.colIndex;
        }

        public int getRowIndex() {
            return this.rowIndex;
        }

        private final int colIndex;
        private final int rowIndex;
    }

    public TableLexer(final Table table) {
        this.stack = new ArrayList<Cursor>();
        this.table = (IntelliTable) table;
        this.colIndex = 0;
        this.rowIndex = 0;
    }

    @Override
    public BaseCell read() {
        if (this.rowIndex >= this.table.getNumberOfRows()) {
            return BaseCell.EndOfStream;
        }

        if (this.colIndex >= this.table.getNumberOfColumns()) {
            this.colIndex = 0;
            this.rowIndex++;
            return BaseCell.EndOfRow;
        }

        final BaseRow row = this.table.getRowAt(this.rowIndex);
        if (row.isEmpty()) {
            this.colIndex = 0;
            this.rowIndex++;
            return BaseCell.EndOfRow;
        }

        final BaseCell cell = row.getCellAt(colIndex);
        colIndex += cell.getMergedCount();

        return cell;
    }

    @Override
    public BaseCell peek() {
        if (this.rowIndex >= this.table.getNumberOfRows()) {
            return BaseCell.EndOfStream;
        }

        if (this.colIndex >= this.table.getNumberOfColumns()) {
            return BaseCell.EndOfRow;
        }

        final BaseRow row = this.table.getRowAt(this.rowIndex);
        if (row.isEmpty()) {
            return BaseCell.EndOfRow;
        }

        return row.getCellAt(colIndex);
    }

    @Override
    public void push() {
        this.stack.add(new Cursor(this.colIndex, this.rowIndex));
    }

    @Override
    public Cursor pop() {
        return this.stack.remove(this.stack.size() - 1);
    }

    @Override
    public void seek(final Cursor c) {
        this.colIndex = c.getColIndex();
        this.rowIndex = c.getRowIndex();
    }

    private final ArrayList<Cursor> stack;
    private final IntelliTable table;
    private int colIndex;
    private int rowIndex;
}
