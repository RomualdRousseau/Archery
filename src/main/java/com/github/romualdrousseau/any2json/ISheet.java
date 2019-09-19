package com.github.romualdrousseau.any2json;

import java.util.List;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

public interface ISheet
{
    public String getName();

    public ISearchBitmap getSearchBitmap(int headerColumns, int headerRows);

	public ITable getTable();

    public ITable findTable(int headerColumns, int headerRows);

    public List<ITable> findTables(int headerColumns, int headerRows);

    public ITable findTableWithItelliTag(ITagClassifier classifier);

    public ITable findTableWithItelliTag(ITagClassifier classifier, String[] requiredTagList);
}

