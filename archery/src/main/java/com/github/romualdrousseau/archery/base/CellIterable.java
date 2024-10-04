package com.github.romualdrousseau.archery.base;

import java.util.Iterator;

import com.github.romualdrousseau.archery.Cell;

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
