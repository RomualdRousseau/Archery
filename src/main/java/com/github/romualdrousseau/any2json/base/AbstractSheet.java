package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.SheetListener;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.IntelliTable;
import com.github.romualdrousseau.any2json.intelli.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.intelli.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.intelli.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.intelli.event.IntelliTableReadyEvent;
import com.github.romualdrousseau.any2json.intelli.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.util.TableGraph;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

import java.util.ArrayList;
import java.util.LinkedList;

import com.github.romualdrousseau.any2json.ITagClassifier;

public abstract class AbstractSheet implements Sheet {

    public abstract int getLastColumnNum(int rowIndex);

    public abstract int getLastRowNum();

    public abstract boolean hasCellDataAt(int colIndex, int rowIndex);

    public abstract String getCellDataAt(int colIndex, int rowIndex);

    public abstract int getNumberOfMergedCellsAt(int colIndex, int rowIndex);

    @Override
    public Table getTable() {
        return this.getTable(null);
    }

    @Override
    public Table getTable(final ITagClassifier classifier) {
        if (this.getLastColumnNum(0) < 0 || this.getLastRowNum() < 0) {
            return null;
        }

        if(classifier == null) {
            this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));
            Table table = new SimpleTable(this, 0, 0, this.getLastColumnNum(0), this.getLastRowNum());
            this.notifyStepCompleted(new IntelliTableReadyEvent(this, table));
            return table;
        }

        this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));

        LinkedList<CompositeTable> tables = new  LinkedList<CompositeTable>();
        tables.add(new CompositeTable(this, 0, 0, this.getLastColumnNum(0), this.getLastRowNum(), classifier));
        this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables));

        LinkedList<DataTable> dataTables = new  LinkedList<DataTable>();
        dataTables.add(new DataTable(tables.getFirst()));
        this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables));

        TableGraph root = new TableGraph();
        root.addChild(new TableGraph(dataTables.getFirst()));
        this.notifyStepCompleted(new TableGraphBuiltEvent(this, root));

        Table table = new IntelliTable(root, classifier);
        this.notifyStepCompleted(new IntelliTableReadyEvent(this, table));

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

    private final ArrayList<SheetListener> listeners = new ArrayList<SheetListener>();
}
