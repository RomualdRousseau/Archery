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
        Table result = null;
        final int lastColumnNum = this.getLastColumnNum(0);
        final int lastRowNum = this.getLastRowNum();
        if (lastColumnNum >= 0 && lastRowNum >= 0) {
            this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));

            if(classifier == null) {
                result = new SimpleTable(this, 0, 0, lastColumnNum, lastRowNum);
                this.notifyStepCompleted(new IntelliTableReadyEvent(this, result));
            } else {
                LinkedList<CompositeTable> tables = new  LinkedList<CompositeTable>();
                tables.add(new CompositeTable(this, 0, 0, lastColumnNum, lastRowNum, classifier));
                this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables));

                LinkedList<DataTable> dataTables = new  LinkedList<DataTable>();
                dataTables.add(new DataTable(tables.getFirst()));
                this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables));

                TableGraph root = new TableGraph();
                root.addChild(new TableGraph(dataTables.getFirst()));
                this.notifyStepCompleted(new TableGraphBuiltEvent(this, root));

                result = new IntelliTable(root, classifier);
                this.notifyStepCompleted(new IntelliTableReadyEvent(this, result));
            }
        }

        return result;
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
