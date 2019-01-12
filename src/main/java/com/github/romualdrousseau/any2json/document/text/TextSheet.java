package com.github.romualdrousseau.any2json.document.text;

import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.any2json.ITable;

class TextSheet implements ISheet
{
	public TextSheet(String name, ITable table) {
		this.name = name;
		this.table = table;
	}

	public String getName() {
		return this.name;
	}

	public ITable getTable() {
		return this.table;
	}

	public ITable findTable(int headerColumns, int headerRows) {
		return this.table;
	}

	private String name;
	private ITable table;
}
