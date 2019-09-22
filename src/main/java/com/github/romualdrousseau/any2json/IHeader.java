package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;

public interface IHeader {
    Vector getWordVector();

    Vector getEntityVector();

    Vector getConflictVector(boolean checkForConflicts);

    String getName();

    String getCleanName();

    String getValue();

    IHeader setName(String name);

    int getColumnIndex();

    IHeader setColumnIndex(int columnIndex);

    int getNumberOfCells();

    IHeader setNumberOfCells(int numberOfCells);

    boolean hasTag();

    HeaderTag getTag();

    IHeader setTag(HeaderTag tag);

    ITable getTable();

    IHeader setTable(ITable table);

    ITagClassifier getTagClassifier();

    IHeader setTagClassifier(ITagClassifier classifier);

    void resetTag();

    void updateTag(boolean checkForConflicts);

    DataRow buildRow(String tagValue, IHeader[] conflicts, boolean ensureWordsExists);

    void chain(IHeader other);

    IHeader next();
}
