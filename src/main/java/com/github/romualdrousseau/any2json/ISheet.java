package com.github.romualdrousseau.any2json;

import java.util.List;

import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

public interface ISheet {

    String getName();

    ISearchBitmap getSearchBitmap(int headerColumns, int headerRows);

	ITable getTable();

    ITable findTable(int headerColumns, int headerRows);

    List<ITable> findTables(int headerColumns, int headerRows);

    ITable findTableWithIntelliTag(ITagClassifier classifier);
}

