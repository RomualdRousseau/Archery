package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;

public class CellIterable implements Iterable<Cell>
{
	public CellIterable(AbstractRow row) {
		this.row = row;
	}

	public Iterator<Cell> iterator() {
		return new CellIterator(this.row);
	}

	private AbstractRow row;
}
