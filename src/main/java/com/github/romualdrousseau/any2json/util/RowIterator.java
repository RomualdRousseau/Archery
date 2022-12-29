package com.github.romualdrousseau.any2json.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseTable;
import com.github.romualdrousseau.any2json.base.BaseRow;

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

	private BaseTable table;
	private int currRowIdx;
}

