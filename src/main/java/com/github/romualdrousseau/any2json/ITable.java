package com.github.romualdrousseau.any2json;

public interface ITable {
    int getGroupId();

    boolean isMetaTable();

    int getNumberOfMetaTables();

    ITable getMetaTableAt(int tableIndex);

    Iterable<ITable> metatables();

    int getNumberOfMetas();

    IRow getMetaRowAt(int rowIndex);

    boolean hasHeaders();

    void clearHeaders();

    int getNumberOfHeaders();

    IHeader getHeaderAt(int colIndex);

    IHeader getHeaderByTag(String tagName);

    Iterable<IHeader> headers();

    int getNumberOfColumns();

    int getNumberOfRows();

    IRow getRowAt(int rowIndex);

    Iterable<IRow> rows();

    void resetHeaderTags();

    void updateHeaderTags(ITagClassifier classifier);
}
