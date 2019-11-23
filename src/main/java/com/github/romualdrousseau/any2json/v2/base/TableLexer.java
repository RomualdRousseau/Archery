package com.github.romualdrousseau.any2json.v2.base;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.Table;
import com.github.romualdrousseau.any2json.v2.layex.Lexer;

public class TableLexer implements Lexer<AbstractCell, TableLexer.Cursor> {

    class Cursor {

        public Cursor(int colIndex, int rowIndex) {
            this.colIndex = colIndex;
            this.rowIndex = rowIndex;
        }

        public int getColIndex() {
            return this.colIndex;
        }

        public int getRowIndex() {
            return this.rowIndex;
        }

        private int colIndex;
        private int rowIndex;
    }

    public TableLexer(Table table) {
        this.stack = new ArrayList<Cursor>();
        this.table = (AbstractTable) table;
        this.colIndex = 0;
        this.rowIndex = 0;
    }

    @Override
    public AbstractCell read() {
        if(this.rowIndex >= this.table.getNumberOfRows()) {
            return AbstractCell.EndOfStream;
        }

        if(this.colIndex >= this.table.getNumberOfColumns()) {
            this.colIndex = 0;
            this.rowIndex++;
            return AbstractCell.EndOfRow;
        }

        AbstractRow row = this.table.getRowAt(this.rowIndex);
        if(row.isEmpty()) {
            this.colIndex = 0;
            this.rowIndex++;
            return AbstractCell.EndOfRow;
        }

        AbstractCell cell = row.getCellAt(colIndex);
        colIndex += cell.getMergedCount();

        return cell;
    }

    @Override
    public AbstractCell peek() {
        if(this.rowIndex >= this.table.getNumberOfRows()) {
            return AbstractCell.EndOfStream;
        }

        if(this.colIndex >= this.table.getNumberOfColumns()) {
            return AbstractCell.EndOfRow;
        }

        AbstractRow row = this.table.getRowAt(this.rowIndex);
        if(row.isEmpty()) {
            return AbstractCell.EndOfRow;
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
    public void seek(Cursor c) {
        this.colIndex = c.getColIndex();
        this.rowIndex = c.getRowIndex();
    }

    private ArrayList<Cursor> stack;
    private AbstractTable table;
    private int colIndex;
    private int rowIndex;
}
