package com.github.romualdrousseau.any2json.document.text;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.TableHeader;

class TextRow extends Row
{
	public TextRow(String[] tokens) {
		this.cells = tokens;
	}

	public int getNumberOfCells() {
		return this.cells.length;
    }

    public int getNumberOfMergedCellsAt(int i) {
        return 1;
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
