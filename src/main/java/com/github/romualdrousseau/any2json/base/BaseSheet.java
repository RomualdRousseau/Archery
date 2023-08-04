package com.github.romualdrousseau.any2json.base;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.SheetListener;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.classifier.SimpleClassifierBuilder;
import com.github.romualdrousseau.any2json.event.TableReadyEvent;
import com.github.romualdrousseau.any2json.simple.SimpleTable;
import com.github.romualdrousseau.shuju.util.CollectionUtils;

public abstract class BaseSheet implements Sheet {

    public abstract Table parseAllTables();

    public BaseSheet(final SheetStore store) {
        this.sheetStore = store;
        this.storeLastColumnNum = this.computeLastColumnNum();
        this.columnMask = CollectionUtils.mutableRange(0, this.storeLastColumnNum + 1);
        this.rowMask = CollectionUtils.mutableRange(0, this.sheetStore.getLastRowNum() + 1);
        this.classifierFactory = new SimpleClassifierBuilder().build();
    }

    @Override
    public String getName() {
        return this.sheetStore.getName();
    }

    @Override
    public int getLastRowNum() {
        return this.rowMask.size() - 1;
    }

    @Override
    public int getLastColumnNum() {
        return this.columnMask.size() - 1;
    }

    @Override
    public Table getTable() {
        if (this.sheetStore.getLastRowNum() <= 0 || this.getLastColumnNum() <= 0) {
            return null;
        }
        final Table table;
        if (this.classifierFactory.getLayoutClassifier().isPresent()) {
            table = this.parseAllTables();
        } else {
            table = this.parseOneTable();
        }
        this.notifyStepCompleted(new TableReadyEvent(this, table));
        return table;
    }

    @Override
    public void setClassifierFactory(final ClassifierFactory classifierFactory) {
        if (classifierFactory == null) {
            this.classifierFactory = new SimpleClassifierBuilder().build();
        }
        else {
            this.classifierFactory = classifierFactory;
        }
    }

    @Override
    public void addSheetListener(final SheetListener listener) {
        this.listeners.add(listener);
    }

    public SheetStore getSheetStore() {
        return this.sheetStore;
    }

    public ClassifierFactory getClassifierFactory() {
        return this.classifierFactory;
    }

    public int getLastColumnNum(final int rowIndex) {
        final int translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return -1;
        }
        return this.sheetStore.getLastColumnNum(translatedRow) - (this.storeLastColumnNum - this.columnMask.size() + 1);
    }

    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return false;
        }
        final int translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return false;
        }
        return this.sheetStore.hasCellDataAt(translatedColumn, translatedRow);
    }

    public boolean hasCellDecorationAt(final int colIndex, final int rowIndex) {
        final int translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return false;
        }
        final int translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return false;
        }
        return this.sheetStore.hasCellDecorationAt(translatedColumn, translatedRow);
    }

    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return null;
        }
        final int translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return null;
        }
        return this.sheetStore.getCellDataAt(translatedColumn, translatedRow);
    }

    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        if (this.unmergedAll) {
            return 1;
        }
        final int translatedColumn = this.translateColumn(colIndex);
        if (translatedColumn < 0) {
            return 1;
        }
        final int translatedRow = this.translateRow(rowIndex);
        if (translatedRow < 0) {
            return 1;
        }
        return this.sheetStore.getNumberOfMergedCellsAt(translatedColumn, translatedRow);
    }

    public void patchCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2,
            final String value) {
        final int translatedColumn1 = this.translateColumn(colIndex1);
        if (translatedColumn1 < 0) {
            return;
        }
        final int translatedRow1 = this.translateRow(rowIndex1);
        if (translatedRow1 < 0) {
            return;
        }
        final int translatedColumn2 = this.translateColumn(colIndex2);
        if (translatedColumn2 < 0) {
            return;
        }
        final int translatedRow2 = this.translateRow(rowIndex2);
        if (translatedRow2 < 0) {
            return;
        }
        this.sheetStore.patchCell(translatedColumn1, translatedRow1, translatedColumn2, translatedRow2, value, this.unmergedAll);
    }

    public void unmergeAll() {
        this.unmergedAll = true;
    }

    public boolean notifyStepCompleted(final SheetEvent e) {
        for (final SheetListener listener : listeners) {
            listener.stepCompleted(e);
        }
        return !e.isCanceled();
    }

    public float getBitmapThreshold() {
        return this.bitmapThreshold;
    }

    public void setBitmapThreshold(final float bitmapThreshold) {
        this.bitmapThreshold = bitmapThreshold;
    }

    protected void markColumnAsNull(final int colIndex) {
        if (colIndex < this.columnMask.size()) {
            this.columnMask.set(colIndex, null);
        }
    }

    protected void removeAllNullColumns() {
        this.columnMask.removeIf(i -> i == null);
    }

    protected void markRowAsNull(final int rowIndex) {
        if (rowIndex < this.rowMask.size()) {
            this.rowMask.set(rowIndex, null);
        }
    }

    protected void removeAllNullRows() {
        this.rowMask.removeIf(i -> i == null);
    }

    private int translateColumn(final int colIndex) {
        if (colIndex < 0 || colIndex >= this.columnMask.size()) {
            return -1;
        }
        return this.columnMask.get(colIndex);
    }

    private int translateRow(final int rowIndex) {
        if (rowIndex < 0 || rowIndex >= this.rowMask.size()) {
            return -1;
        }
        return this.rowMask.get(rowIndex);
    }

    private int computeLastColumnNum() {
        if (this.sheetStore.getLastRowNum() < 0) {
            return -1;
        }
        int result = this.sheetStore.getLastColumnNum(0);
        for (int i = 1; i <= Math.min(DocumentFactory.DEFAULT_SAMPLE_COUNT, this.sheetStore.getLastRowNum()); i++) {
            result = Math.max(result, this.sheetStore.getLastColumnNum(i));
        }
        return result;
    }

    private Table parseOneTable() {
        return new SimpleTable(this, 0, 0, this.getLastColumnNum(), this.getLastRowNum());
    }

    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
    private final SheetStore sheetStore;
    private final List<Integer> rowMask;
    private final List<Integer> columnMask;
    private final int storeLastColumnNum;
    private ClassifierFactory classifierFactory;
    private boolean unmergedAll = false;
    private float bitmapThreshold = 0.5f;
}
