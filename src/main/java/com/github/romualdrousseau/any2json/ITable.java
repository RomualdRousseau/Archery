package com.github.romualdrousseau.any2json;

import java.util.List;

public interface ITable {
    int getGroupId();

    boolean isMetaTable();

    int getNumberOfMetas();

    List<ITable> getMetaTables();

    IRow getMetaAt(int i);

    boolean hasHeaders();

    Iterable<TableHeader> headers();

    int getNumberOfHeaders();

    TableHeader getHeaderAt(int colIndex);

    TableHeader getHeaderByTag(String tagName);

    TableHeader getHeaderByCleanName(String cleanName);

    void clearHeaders();

    //int getFirstColumn();

    int getNumberOfColumns();

    Iterable<IRow> rows();

    //int getFirstRow();

    int getNumberOfRows();

    IRow getRowAt(int i);

    void resetHeaderTags();

    void updateHeaderTags(ITagClassifier classifier);
}
