package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class RowIterator implements Iterator<Row>
{
	public RowIterator(AbstractTable table) {
		this.table = table;
		this.currRowIdx = 0;
	}

	public boolean hasNext() {
		return this.currRowIdx < this.table.getNumberOfRows();
	}

	public Row next() {
		return this.table.getRowAt(this.currRowIdx++);
	}

	private AbstractTable table;
	private int currRowIdx;
}

