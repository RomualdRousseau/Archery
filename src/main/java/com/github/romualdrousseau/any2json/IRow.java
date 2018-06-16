package com.github.romualdrousseau.any2json;

public abstract class IRow
{
	public abstract int getNumberOfCells();

	public abstract String getCellValue(TableHeader header);

	public abstract String getCellValueAt(int i);
}