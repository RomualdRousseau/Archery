package com.github.romualdrousseau.archery.base;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import com.github.romualdrousseau.archery.Row;

public class RowIterator implements Iterator<Row>
{
	public RowIterator(BaseTable table) {
		this.table = table;
		this.currRowIdx = 0;
	}

	public boolean hasNext() {
		return this.currRowIdx < this.table.getNumberOfRows();
	}

	public Row next() {
        BaseRow row = this.table.getRowAt(this.currRowIdx++);
        row.setRowNum(this.currRowIdx);
		return row;
	}

    public Spliterator<Row> spliterator() {
        return Spliterators.spliterator(this, this.table.getNumberOfRows(), Spliterator.IMMUTABLE);
    }

	private BaseTable table;
	private int currRowIdx;
}
