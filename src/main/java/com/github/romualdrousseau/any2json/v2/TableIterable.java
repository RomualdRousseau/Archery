package com.github.romualdrousseau.any2json.v2;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.base.Table;

public class TableIterable implements Iterable<ICell>
{
	public TableIterable(Table table) {
		this.table = table;
	}

	public Iterator<ICell> iterator() {
		return new TableIterator(this.table);
	}

	private Table table;
}
