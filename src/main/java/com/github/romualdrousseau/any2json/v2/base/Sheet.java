package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.ISheet;
import com.github.romualdrousseau.any2json.v2.ITable;
import com.github.romualdrousseau.any2json.ITagClassifier;

public abstract class Sheet implements ISheet {

    @Override
    public ITable getTable(ITagClassifier classifier) {
        ITable result = null;

        int lastColumnNum = this.getLastColumnNum(0, 0);
        int lastRowNum = this.getLastRowNum();
        if (lastColumnNum > 0 && lastRowNum > 0) {
            result = new Table(this, 0, 0, lastColumnNum, lastRowNum, classifier);
        }

        return result;
    }

    public abstract int getLastColumnNum(int colIndex, int rowIndex);

    public abstract int getLastRowNum();

    public abstract String getInternalCellValueAt(int colIndex, int rowIndex);

    public abstract int getNumberOfMergedCellsAt(int colIndex, int rowIndex);
}
