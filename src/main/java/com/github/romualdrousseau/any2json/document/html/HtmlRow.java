package com.github.romualdrousseau.any2json.document.html;

import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.Row;

class HtmlRow extends Row
{
	public HtmlRow(String[] cells) {
		this.cells = cells;
	}

	public int getNumberOfCells() {
		return this.cells.length;
	}

	public String getCellValue(TableHeader header) {
		if(header == null) {
			throw new IllegalArgumentException();
		}

		return getCellValueAt(header.getColumnIndex());
	}

	public String getCellValueAt(int i) {
		if(i < 0 || i >= getNumberOfCells()) {
			throw new ArrayIndexOutOfBoundsException(i);
		}

		return this.cells[i];
	}

	private String[] cells = null;
}