package com.github.romualdrousseau.any2json.v2;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.base.Cell;
import com.github.romualdrousseau.any2json.v2.base.Row;
import com.github.romualdrousseau.any2json.v2.base.Table;

public class TableIterator implements Iterator<ICell>
{
	public TableIterator(Table table) {
        this.table = table;
        this.colIndex = 0;
        this.rowIndex = 0;
    }

    public boolean hasNext() {
		return this.rowIndex < this.table.getNumberOfRows();
	}

	public ICell next() {
        if(this.rowIndex >= this.table.getNumberOfRows()) {
            return Cell.EndOfStream;
        }

        if(this.colIndex >= this.table.getNumberOfColumns()) {
            this.colIndex = 0;
            this.rowIndex++;
            return Cell.EndOfRow;
        }

        Row row = this.table.getRowAt(this.rowIndex);
        if(row.isEmpty()) {
            this.rowIndex++;
            return Cell.EndOfRow;
        }

        Cell cell = row.getCellAt(colIndex);
        colIndex += cell.getMergedCount();

        return cell;
	}

    private Table table;
    private int colIndex;
    private int rowIndex;
}
