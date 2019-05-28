package com.github.romualdrousseau.any2json;

import java.util.List;

public interface ISheet
{
	public String getName();

	public ITable getTable();

    public ITable findTable(int headerColumns, int headerRows);

    public List<ITable> findTables(int headerColumns, int headerRows);

    public ITable findTableWithItelliTag(ITagClassifier classifier);
}
