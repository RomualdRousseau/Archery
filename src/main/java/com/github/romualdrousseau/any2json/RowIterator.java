package com.github.romualdrousseau.any2json;

import java.util.Iterator;

public class RowIterator implements Iterator<IRow>
{
	public RowIterator(ITable table) {
		this.table = table;
		this.currRowIdx = 0;
	}

	public boolean hasNext() {
		return this.currRowIdx < this.table.getNumberOfRows();
	}

	public IRow next() {
		return this.table.getRowAt(this.currRowIdx++);
	}

	private ITable table;
	private int currRowIdx;
}

