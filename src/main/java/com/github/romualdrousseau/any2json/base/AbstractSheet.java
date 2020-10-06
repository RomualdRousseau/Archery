package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.SheetListener;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.event.TableReadyEvent;
import com.github.romualdrousseau.any2json.simple.SimpleTable;

import java.util.ArrayList;

public abstract class AbstractSheet implements Sheet {

    @Override
    public Table getTable() {
        return this.getTable(null);
    }

    @Override
    public Table getTable(final ITagClassifier classifier) {
        if (this.getLastRowNum() < 0 || this.getLastColumnNum() < 0) {
            return null;
        }
        this.classifier = classifier;
        Table table = (this.classifier == null) ? this.createSimpleTable() : this.createIntelliTable();
        this.notifyStepCompleted(new TableReadyEvent(this, table));
        return table;
    }

    @Override
    public void addSheetListener(final SheetListener listener) {
        this.listeners.add(listener);
    }

    public boolean notifyStepCompleted(final SheetEvent e) {
        for (final SheetListener listener : listeners) {
            listener.stepCompleted(e);
        }
        return !e.isCanceled();
    }

    public Table createSimpleTable() {
        this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));
        return new SimpleTable(this, 0, 0, this.getLastColumnNum(), this.getLastRowNum());
    }

    public int getLastColumnNum() {
        int result = this.getLastColumnNum(0);
        for (int i = 1; i < Math.min(DocumentFactory.DEFAULT_SAMPLE_COUNT, this.getLastRowNum()); i++) {
            result = Math.max(result, this.getLastColumnNum(i));
        }
        return result;
    }

    public abstract Table createIntelliTable();

    public abstract int getLastColumnNum(int rowIndex);

    public abstract int getLastRowNum();

    public abstract boolean hasCellDataAt(int colIndex, int rowIndex);

    public abstract boolean hasCellDecorationAt(int colIndex, int rowIndex);

    public abstract String getCellDataAt(int colIndex, int rowIndex);

    public abstract int getNumberOfMergedCellsAt(int colIndex, int rowIndex);

    protected ITagClassifier classifier = null;

    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
}
