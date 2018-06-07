package org.any2json.document.html;

import org.any2json.ISheet;
import org.any2json.ITable;

class HtmlSheet extends ISheet
{
	public HtmlSheet(String name, ITable table) {
		m_name = name;
		m_table = table;
	}

	public String getName() {
		return m_name;
	}

	public ITable getTable() {
		return m_table;
	}

	private String m_name;
	private ITable m_table;
}
