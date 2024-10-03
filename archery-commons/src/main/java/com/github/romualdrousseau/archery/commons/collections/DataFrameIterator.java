package com.github.romualdrousseau.archery.commons.collections;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public class DataFrameIterator implements Iterator<Row> {
    private final DataFrame dataFrame;

	private int curr;

	public DataFrameIterator(final DataFrame dataFrame) {
		this.dataFrame = dataFrame;
		this.curr = 0;
	}

	public boolean hasNext() {
		return this.curr < this.dataFrame.getRowCount();
	}

	public Row next() {
        return this.dataFrame.getRow(this.curr++);
	}

    public Spliterator<Row> spliterator() {
        return Spliterators.spliterator(this, this.dataFrame.getRowCount(), Spliterator.IMMUTABLE);
    }
}
