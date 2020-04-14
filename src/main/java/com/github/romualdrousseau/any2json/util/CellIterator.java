package com.github.romualdrousseau.any2json.util;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.Cell;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseRow;

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

