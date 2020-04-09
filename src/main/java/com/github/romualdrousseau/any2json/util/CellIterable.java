package com.github.romualdrousseau.any2json.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.base.BaseRow;

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
