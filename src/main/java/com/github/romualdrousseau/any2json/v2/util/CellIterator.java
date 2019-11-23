package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;

public class CellIterator implements Iterator<Cell>
{
	public CellIterator(AbstractRow row) {
		this.row = row;
		this.currColIdx = 0;
	}

	public boolean hasNext() {
		return this.currColIdx < this.row.getTable().getNumberOfColumns();
	}

	public Cell next() {
        Cell cell = this.row.getCellAt(this.currColIdx);
        this.currColIdx += cell.getMergedCount();
        return cell;
	}

	private AbstractRow row;
	private int currColIdx;
}

