package com.github.romualdrousseau.any2json.document.text;

import java.io.IOException;
import java.io.BufferedReader;

import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.any2json.ITable;

class TextSheet extends ISheet
{
	public TextSheet(String name, ITable table) {
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
