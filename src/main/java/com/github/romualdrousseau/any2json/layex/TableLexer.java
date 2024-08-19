package com.github.romualdrousseau.any2json.layex;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.collections4.map.LRUMap;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseTable;

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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Cursor)) {
                return false;
            }
            final var other = (Cursor) o;
            return other != null && this.colIndex == other.colIndex && this.rowIndex == other.rowIndex;
        }

        @Override
        public int hashCode() {
            return this.colIndex ^ this.rowIndex;
        }

        private final int colIndex;
        private final int rowIndex;
    }

    public TableLexer(final Table table, final int rowOffset) {
        this.lruCache = new LRUMap<>(1024);
        this.stack = new ArrayDeque<>();
        this.table = (BaseTable) table;
        this.rowOffset = rowOffset;
        this.cursor = new Cursor(0, this.rowOffset);
    }

    @Override
    public TableLexer reset() {
        this.stack.clear();
        this.cursor = new Cursor(0, this.rowOffset);
        return this;
    }

    @Override
    public BaseCell read() {
        final var cell = this.peek();
        if (cell == BaseCell.EndOfRow) {
            this.cursor = new Cursor(0, this.cursor.getRowIndex() + 1);
        } else {
            this.cursor = new Cursor(this.cursor.getColIndex() + cell.getMergedCount(), this.cursor.getRowIndex());
        }
        return cell;
    }

    @Override
    public BaseCell peek() {
        return this.lruCache.computeIfAbsent(this.cursor, (x) -> {
            if (x.getRowIndex() >= this.table.getNumberOfRows()) {
                return BaseCell.EndOfStream;
            }
            if (x.getColIndex() >= this.table.getNumberOfColumns()) {
                return BaseCell.EndOfRow;
            }
            final var row = this.table.getRowAt(x.getRowIndex());
            return (!row.isEmpty()) ? row.getCellAt(x.getColIndex()) : BaseCell.EndOfRow;
        });
    }

    @Override
    public void push() {
        this.stack.push(this.cursor);
    }

    @Override
    public Cursor pop() {
        return this.stack.pop();
    }

    @Override
    public void seek(final Cursor c) {
        this.cursor = c;
    }

    private final LRUMap<Cursor, BaseCell> lruCache;
    private final Deque<Cursor> stack;
    private final BaseTable table;
    private final int rowOffset;
    private Cursor cursor;
}
