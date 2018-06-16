package com.github.romualdrousseau.any2json.document.text;


import com.github.romualdrousseau.any2json.IRow;
import com.github.romualdrousseau.any2json.TableHeader;

class TextRow extends IRow
{
	public TextRow(String[] tokens) {
		m_row = tokens;
	}

	public int getNumberOfCells() {
		return m_row.length;
	}

	public String getCellValue(TableHeader header) {
		return getCellValueAt(header.getColumnIndex());
	}

	public String getCellValueAt(int i) {
		return m_row[i];
	}

	private String[] m_row = null;
}
