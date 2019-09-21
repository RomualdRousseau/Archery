package com.github.romualdrousseau.any2json;

import java.util.Iterator;

public class RowIterable implements Iterable<IRow>
{
	public RowIterable(ITable table) {
		this.table = table;
	}

	public Iterator<IRow> iterator() {
		return new RowIterator(this.table);
	}

	private ITable table;
}
