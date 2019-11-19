package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;

public interface IHeader {
    Vector getWordVector();

    Vector getEntityVector();

    Vector getConflictVector(boolean checkForConflicts);

    String getName();

    String getCleanName();

    IHeader setName(String name);

    String getValue();

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
