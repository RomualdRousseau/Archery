package com.github.romualdrousseau.any2json.document.text;

import com.github.romualdrousseau.any2json.TableRow;
import com.github.romualdrousseau.any2json.IHeader;

class TextRow extends TableRow
{
	public TextRow(TextTable table, String[] tokens, int groupId) {
        this.table = table;
        this.cells = tokens;
        this.groupId = groupId;
    }

    public TextTable getTable() {
        return this.table;
    }

	public int getNumberOfCells() {
		return this.cells.length;
    }

    public int getNumberOfMergedCellsAt(int i) {
        return 1;
    }

	public String getCellValue(IHeader header) {
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

    private TextTable table;
	private String[] cells;
}
