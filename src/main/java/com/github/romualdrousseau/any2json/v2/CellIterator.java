package com.github.romualdrousseau.any2json.v2;

import java.util.Iterator;

import com.github.romualdrousseau.any2json.v2.base.Row;

public class CellIterator implements Iterator<ICell>
{
	public CellIterator(Row row) {
		this.row = row;
		this.currColIdx = 0;
	}

	public boolean hasNext() {
		return this.currColIdx < this.row.getTable().getNumberOfColumns();
	}

	public ICell next() {
        ICell cell = this.row.getCellAt(this.currColIdx);
        this.currColIdx += cell.getMergedCount();
        return cell;
	}

	private Row row;
	private int currColIdx;
}

