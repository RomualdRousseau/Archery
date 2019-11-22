package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.base.Table;

public class RowIterator implements Iterator<IRow>
{
	public RowIterator(Table table) {
		this.table = table;
		this.currRowIdx = 0;
	}

	public boolean hasNext() {
		return this.currRowIdx < this.table.getNumberOfRows();
	}

	public IRow next() {
		return this.table.getRowAt(this.currRowIdx++);
	}

	private Table table;
	private int currRowIdx;
}

