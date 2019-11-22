package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.ICell;
import com.github.romualdrousseau.any2json.v2.base.Row;

public class CellIterable implements Iterable<ICell>
{
	public CellIterable(Row row) {
		this.row = row;
	}

	public Iterator<ICell> iterator() {
		return new CellIterator(this.row);
	}

	private Row row;
}
