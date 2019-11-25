package com.github.romualdrousseau.any2json.v2.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.Cell;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;

public class CellIterator implements Iterator<Cell>
{
	public CellIterator(BaseRow row) {
		this.row = row;
		this.currColIdx = 0;
	}

	public boolean hasNext() {
		return this.currColIdx < this.row.getTable().getNumberOfColumns();
	}

	public Cell next() {
        BaseCell cell = this.row.getCellAt(this.currColIdx);
        this.currColIdx += cell.getMergedCount();
        return cell;
	}

	private BaseRow row;
	private int currColIdx;
}

