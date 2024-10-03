package com.github.romualdrousseau.archery.commons.collections;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

public class RowIterator implements Iterator<String> {
    private final String[] row;

	private int curr;

	public RowIterator(final String[] row) {
		this.row = row;
		this.curr = 0;
	}

	public boolean hasNext() {
		return this.curr < this.row.length;
	}

	public String next() {
        return this.row[this.curr++];
	}

    public Spliterator<String> spliterator() {
        return Spliterators.spliterator(this, this.row.length, Spliterator.IMMUTABLE);
    }
}
