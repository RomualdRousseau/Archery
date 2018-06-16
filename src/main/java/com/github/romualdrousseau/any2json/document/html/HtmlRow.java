package com.github.romualdrousseau.any2json.document.html;

import com.github.romualdrousseau.any2json.TableHeader;
import com.github.romualdrousseau.any2json.IRow;

class HtmlRow extends IRow
{
	public HtmlRow(String[] rows) {
		m_row = rows;
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