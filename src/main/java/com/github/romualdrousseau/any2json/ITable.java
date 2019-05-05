package com.github.romualdrousseau.any2json;

public interface ITable {
    Iterable<TableHeader> headers();

    int getNumberOfHeaders();

    TableHeader getHeaderAt(int colIndex);

    TableHeader getHeaderByTag(String tagName);

    boolean hasHeaders();

    void clearHeaders();

    int getNumberOfColumns();

    Iterable<IRow> rows();

    int getNumberOfRows();

    IRow getRowAt(int i);

    void resetHeaderTags();

    void updateHeaderTags(ITagClassifier classifier);
}
