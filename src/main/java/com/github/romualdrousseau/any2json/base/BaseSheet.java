package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.SheetListener;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.event.TableReadyEvent;
import com.github.romualdrousseau.any2json.util.SheetStore;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSheet implements Sheet {

    public abstract Table createIntelliTable();

    public BaseSheet(final SheetStore store) {
        this.sheetStore = store;
        this.realLastColumnNum = this.computeLastColumnNum();
        this.columnMask = this.mutableRange(0, this.realLastColumnNum);
        this.rowMask = this.mutableRange(0, this.getSheetStore().getLastRowNum());
    }

    @Override
    public String getName() {
        return this.sheetStore.getName();
    }

    @Override
    public Table getTable(final ClassifierFactory classifierFactory) {
        assert(classifierFactory != null);
        this.classifierFactory = classifierFactory;

        if (this.sheetStore.getLastRowNum() < 0 || this.getLastColumnNum() < 0) {
            return null;
        }

        final Table table = this.classifierFactory.getLayoutClassifier().isPresent() ? this.createIntelliTable() : this.createSimpleTable();
        this.notifyStepCompleted(new TableReadyEvent(this, table));
        return table;
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

    public int getLastColumnNum() {
        return this.columnMask.size();
    }

    public int getLastColumnNum(final int rowIndex) {
        final int translatedRow = this.rowMask.get(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getSheetStore().getLastRowNum()) {
            return -1;
        }
        return this.sheetStore.getLastColumnNum(translatedRow) - (this.realLastColumnNum - this.columnMask.size());
    }

    public int getLastRowNum() {
        return this.rowMask.size();
    }

    public boolean hasCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowMask.get(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getSheetStore().getLastRowNum()) {
            return false;
        }
        final int translatedColumn = this.columnMask.get(colIndex);
        if (translatedColumn < 0 || translatedColumn > this.getSheetStore().getLastColumnNum(translatedRow)) {
            return false;
        }
        return this.getSheetStore().hasCellDataAt(translatedColumn, translatedRow);
    }

    public boolean hasCellDecorationAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowMask.get(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getSheetStore().getLastRowNum()) {
            return false;
        }
        final int translatedColumn = this.columnMask.get(colIndex);
        if (translatedColumn < 0 || translatedColumn > this.getSheetStore().getLastColumnNum(translatedRow)) {
            return false;
        }
        return this.getSheetStore().hasCellDecorationAt(translatedColumn, translatedRow);
    }

    public String getCellDataAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowMask.get(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getSheetStore().getLastRowNum()) {
            return null;
        }
        final int translatedColumn = this.columnMask.get(colIndex);
        if (translatedColumn < 0 || translatedColumn > this.getSheetStore().getLastColumnNum(translatedRow)) {
            return null;
        }
        return this.getSheetStore().getCellDataAt(translatedColumn, translatedRow);
    }

    public int getNumberOfMergedCellsAt(final int colIndex, final int rowIndex) {
        final int translatedRow = this.rowMask.get(rowIndex);
        if (translatedRow < 0 || translatedRow > this.getSheetStore().getLastRowNum()) {
            return 1;
        }
        final int translatedColumn = this.columnMask.get(colIndex);
        if (translatedColumn < 0 || translatedColumn > this.getSheetStore().getLastColumnNum(translatedRow)) {
            return 1;
        }
        return this.getSheetStore().getNumberOfMergedCellsAt(translatedColumn, translatedRow);
    }

    public void copyCell(final int colIndex1, final int rowIndex1, final int colIndex2, final int rowIndex2) {
        final int translatedRow1 = this.rowMask.get(rowIndex1);
        if (translatedRow1 < 0 || translatedRow1 > this.getSheetStore().getLastRowNum()) {
            return;
        }
        final int translatedColumn1 = this.columnMask.get(colIndex1);
        if (translatedColumn1 < 0 || translatedColumn1 > this.getSheetStore().getLastColumnNum(translatedRow1)) {
            return;
        }
        final int translatedRow2 = this.rowMask.get(rowIndex2);
        if (translatedRow2 < 0 || translatedRow2 > this.getSheetStore().getLastRowNum()) {
            return;
        }
        final int translatedColumn2 = this.columnMask.get(colIndex2);
        if (translatedColumn2 < 0 || translatedColumn2 > this.getSheetStore().getLastColumnNum(translatedRow2)) {
            return;
        }
        this.getSheetStore().copyCell(translatedColumn1, translatedRow1, translatedColumn2, translatedRow2);
    }

    protected void markColumnAsNull(final int colIndex) {
        this.columnMask.set(colIndex, null);
    }

    protected void removeAllNullColumns() {
        this.columnMask.removeIf(i -> i == null);
    }

    protected void markRowAsNull(final int colIndex) {
        this.rowMask.set(colIndex, null);
    }

    protected void removeAllNullRows() {
        this.rowMask.removeIf(i -> i == null);
    }

    protected boolean notifyStepCompleted(final SheetEvent e) {
        for (final SheetListener listener : listeners) {
            listener.stepCompleted(e);
        }
        return !e.isCanceled();
    }

    private int computeLastColumnNum() {
        int result = this.sheetStore.getLastColumnNum(0);
        for (int i = 1; i < Math.min(DocumentFactory.DEFAULT_SAMPLE_COUNT, this.sheetStore.getLastRowNum()); i++) {
            result = Math.max(result, this.sheetStore.getLastColumnNum(i));
        }
        return result;
    }

    private Table createSimpleTable() {
        this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));
        return new SimpleTable(this, 0, 0, this.getLastColumnNum(), this.sheetStore.getLastRowNum());
    }

    private List<Integer> mutableRange(int a, int b) {
        List<Integer> result = new ArrayList<Integer>();
        for(int i = a; i < b; i++) {
            result.add(i);
        }
        return result;
    }

    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
    private final SheetStore sheetStore;
    private final List<Integer> rowMask;
    private final List<Integer> columnMask;
    private final int realLastColumnNum;
    private ClassifierFactory classifierFactory;
}
