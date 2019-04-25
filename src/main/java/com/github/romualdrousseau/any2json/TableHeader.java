package com.github.romualdrousseau.any2json;

public class TableHeader
{
	public String getName() {
		return this.name;
	}

	public TableHeader setName(String name) {
		this.name = name;
		return this;
	}

	public int getColumnIndex() {
		return this.columnIndex;
	}

	public TableHeader setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
		return this;
	}

	public int getNumberOfCells() {
		return this.numberOfCells;
	}

	public TableHeader setNumberOfCells(int numberOfCells) {
		this.numberOfCells = numberOfCells;
		return this;
	}

	public boolean hasTag() {
		return this.tag != null;
	}

	public HeaderTag getTag() {
		return this.tag;
	}

	public TableHeader setTag(HeaderTag tag) {
		this.tag = tag;
		return this;
	}

	public ITable getTable() {
		return this.table;
	}

	public TableHeader setTable(ITable table) {
		this.table = table;
		return this;
	}

	public String toString() {
		return this.name;
	}

	private String name;
	private int columnIndex;
	private int numberOfCells;
	private HeaderTag tag;
	private ITable table;
}
