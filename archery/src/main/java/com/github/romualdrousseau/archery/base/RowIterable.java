package com.github.romualdrousseau.archery.base;

import java.util.Iterator;

import com.github.romualdrousseau.archery.Row;

public class RowIterable implements Iterable<Row>
{
	public RowIterable(BaseTable table) {
		this.table = table;
	}

	public Iterator<Row> iterator() {
		return new RowIterator(this.table);
	}

	private BaseTable table;
}
