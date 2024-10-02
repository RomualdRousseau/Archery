package com.github.romualdrousseau.archery.commons.bigdata;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public class DataViewIterator implements Iterator<Row> {
    private final DataView view;

	private int curr;

	public DataViewIterator(final DataView view) {
		this.view = view;
		this.curr = 0;
	}

	public boolean hasNext() {
		return this.curr < this.view.getRowCount();
	}

	public Row next() {
        return this.view.getRow(this.curr++);
	}

    public Spliterator<Row> spliterator() {
        return Spliterators.spliterator(this, this.view.getRowCount(), Spliterator.IMMUTABLE);
    }
}
