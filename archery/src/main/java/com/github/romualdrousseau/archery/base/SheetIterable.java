package com.github.romualdrousseau.archery.base;

import java.util.Iterator;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.Sheet;

public class SheetIterable implements Iterable<Sheet>
{
	public SheetIterable(final Document document) {
		this.document = document;
	}

	public Iterator<Sheet> iterator() {
		return new SheetIterator(this.document);
	}

	private final Document document;
}
