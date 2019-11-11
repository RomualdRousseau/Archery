package com.github.romualdrousseau.any2json;

public interface ITable {
    void enableMetaTable(boolean b);

    void enableIntelliTable(boolean b);

    boolean isMetaTableEnabled();

    boolean isIntelliTableEnabled();

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

    int getFirstColumn();

    int getFirstRow();

    int getLastColumn();

    int getLastRow();

    int getNumberOfColumns();

    int getNumberOfRows();

    IRow getRowAt(int rowIndex);

    Iterable<IRow> rows();

    void resetHeaderTags();

    void updateHeaderTags(ITagClassifier classifier);

    void updateHeaderTags(ITagClassifier classifier, boolean disableCheckValidity);
}
