package com.github.romualdrousseau.any2json;

public interface ISheet
{
	public String getName();

	public ITable getTable();
	
	public ITable findTable(int headerColumns, int headerRows);
}