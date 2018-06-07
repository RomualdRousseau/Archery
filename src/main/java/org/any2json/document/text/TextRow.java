package org.any2json.document.text;


import org.any2json.IRow;
import org.any2json.TableHeader;

class TextRow extends IRow
{
	public TextRow(String[] tokens) {
		m_row = tokens;
	}

	public int getNumberOfCells() {
		return m_row.length;
	}

	public String getCellValue(TableHeader header) {
		return getCellValueAt(header.columnIndex);
	}

	public String getCellValueAt(int i) {
		return m_row[i];
	}

	private String[] m_row = null;
}
