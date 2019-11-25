package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;

public class CellIterable implements Iterable<Cell>
{
	public CellIterable(BaseRow row) {
		this.row = row;
	}

	public Iterator<Cell> iterator() {
		return new CellIterator(this.row);
	}

	private BaseRow row;
}
