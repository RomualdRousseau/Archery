package com.github.romualdrousseau.archery.base;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import com.github.romualdrousseau.archery.Cell;

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

    public Spliterator<Cell> spliterator() {
        return Spliterators.spliterator(this, this.row.getTable().getNumberOfColumns(), Spliterator.IMMUTABLE);
    }

	private BaseRow row;
	private int currColIdx;
}

