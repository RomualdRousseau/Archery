package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.base.Table;

public class RowIterable implements Iterable<IRow>
{
	public RowIterable(Table table) {
		this.table = table;
	}

	public Iterator<IRow> iterator() {
		return new RowIterator(this.table);
	}

	private Table table;
}
