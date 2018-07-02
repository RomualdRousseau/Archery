package com.github.romualdrousseau.any2json.document.html;

import com.github.romualdrousseau.any2json.ISheet;
import com.github.romualdrousseau.any2json.ITable;

class HtmlSheet implements ISheet
{
	public HtmlSheet(String name, ITable table) {
		this.name = name;
		this.table = table;
	}

	public String getName() {
		return this.name;
	}

	public ITable getTable() {
		return this.table;
	}

	private String name;
	private ITable table;
}
