package org.any2json.document.html;

import org.any2json.TableHeader;
import org.any2json.IRow;

class HtmlRow extends IRow
{
	public HtmlRow(String[] rows) {
		m_row = rows;
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