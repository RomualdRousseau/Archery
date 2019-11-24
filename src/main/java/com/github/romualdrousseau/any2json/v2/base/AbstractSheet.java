package com.github.romualdrousseau.any2json.v2.base;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.Table;
import com.github.romualdrousseau.any2json.v2.SheetListener;
import com.github.romualdrousseau.any2json.v2.intelli.DataTable;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;

public abstract class AbstractSheet implements Sheet {

    public abstract int getLastColumnNum(int rowIndex);

    public abstract int getLastRowNum();

    public abstract String getInternalCellValueAt(int colIndex, int rowIndex);

    public abstract int getNumberOfMergedCellsAt(int colIndex, int rowIndex);

    @Override
    public Table getTable(final ITagClassifier classifier) {
        AbstractTable result = null;
        final int lastColumnNum = this.getLastColumnNum(0);
        final int lastRowNum = this.getLastRowNum();
        if (lastColumnNum > 0 && lastRowNum > 0) {
            result = new DataTable(new AbstractTable(this, 0, 0, lastColumnNum, lastRowNum, classifier));
        }
        return result;
    }

    @Override
    public void addSheetListener(final SheetListener listener) {
        this.listeners.add(listener);
    }

    public void notifyStepCompleted(final SheetEvent e) {
        for (final SheetListener listener : listeners) {
            listener.stepCompleted(e);
        }
    }

    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
}
