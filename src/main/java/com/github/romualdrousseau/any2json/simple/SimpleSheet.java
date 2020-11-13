package com.github.romualdrousseau.any2json.simple;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.base.AbstractSheet;
import com.github.romualdrousseau.any2json.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.event.BitmapGeneratedEvent;
import com.github.romualdrousseau.any2json.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.intelli.IntelliTable;
import com.github.romualdrousseau.any2json.util.TableGraph;

public abstract class SimpleSheet extends AbstractSheet {

    @Override
    public Table createIntelliTable() {
        this.notifyStepCompleted(new BitmapGeneratedEvent(this, null));

        final LinkedList<CompositeTable> tables = new  LinkedList<CompositeTable>();
        tables.add(new CompositeTable(this, 0, 0, this.getLastColumnNum(), this.getLastRowNum()));
        this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables));

        LinkedList<DataTable> dataTables = new  LinkedList<DataTable>();
        dataTables.add(new DataTable(tables.getFirst()));
        this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables));

        TableGraph root = new TableGraph();
        root.addChild(new TableGraph(dataTables.getFirst()));
        this.notifyStepCompleted(new TableGraphBuiltEvent(this, root));

        return new IntelliTable(this, root);
    }
}
