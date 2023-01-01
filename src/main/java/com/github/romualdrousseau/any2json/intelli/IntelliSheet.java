package com.github.romualdrousseau.any2json.intelli;

import java.util.List;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.base.SheetParser;
import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.any2json.event.AllTablesExtractedEvent;
import com.github.romualdrousseau.any2json.event.DataTableListBuiltEvent;
import com.github.romualdrousseau.any2json.event.MetaTableListBuiltEvent;
import com.github.romualdrousseau.any2json.event.SheetPreparedEvent;
import com.github.romualdrousseau.any2json.event.TableGraphBuiltEvent;
import com.github.romualdrousseau.any2json.util.Visitable;

public class IntelliSheet extends TransformableSheet {

    public IntelliSheet(SheetStore store, SheetParser parser) {
        super(store);
        this.sheetParser = parser;
    }

    @Override
    public Table parseTables() {
        this.sheetParser.transformSheet(this);
        if (!this.notifyStepCompleted(new SheetPreparedEvent(this))) {
            return null;
        }

        final List<CompositeTable> tables = this.sheetParser.findAllTables(this);
        if (!this.notifyStepCompleted(new AllTablesExtractedEvent(this, tables))) {
            return null;
        }

        final List<DataTable> dataTables = this.sheetParser.getDataTables(this, tables);
        if (!this.notifyStepCompleted(new DataTableListBuiltEvent(this, dataTables))) {
            return null;
        }
        if (dataTables.size() == 0) {
            return null;
        }

        final List<MetaTable> metaTables = this.sheetParser.getMetaTables(this, tables);
        if (!this.notifyStepCompleted(new MetaTableListBuiltEvent(this, metaTables))) {
            return null;
        }

        final CompositeTableGraph root = this.buildTableGraph(metaTables, dataTables);
        if (!this.notifyStepCompleted(new TableGraphBuiltEvent(this, root))) {
            return null;
        }

        return new IntelliTable(this, root);
    }

    private CompositeTableGraph buildTableGraph(final List<MetaTable> metaTables, final List<DataTable> dataTables) {
        final CompositeTableGraph root = new CompositeTableGraph();

        for (final Visitable e : metaTables) {
            e.setVisited(false);
        }

        // First attach all not snapped metaTables to the root nodes
        for (final MetaTable metaTable : metaTables) {
            if (!isJoint(metaTable, dataTables)) {
                root.addChild(new CompositeTableGraph(metaTable));
                metaTable.setVisited(true);
            }
        }

        // Second attach all snapped metaTables to the closest nodes
        for (final MetaTable metaTable : metaTables) {
            if (metaTable.isVisited()) {
                continue;
            }

            final CompositeTableGraph parent = findClosestMetaGraph(root, metaTable, 0, 0);
            parent.addChild(new CompositeTableGraph(metaTable));
            metaTable.setVisited(true);
        }

        // Third attach datatables to the closest metadatas
        for (final DataTable dataTable : dataTables) {
            final CompositeTableGraph parent = findClosestMetaGraph(root, dataTable, 0, 1);
            parent.addChild(new CompositeTableGraph(dataTable));
        }

        return root;
    }

    private boolean isJoint(final MetaTable metaTable, final List<DataTable> dataTables) {
        for (final DataTable dataTable : dataTables) {
            if (distanceBetweenTables(metaTable, dataTable) == 0) {
                return true;
            }
        }
        return false;
    }

    private CompositeTableGraph findClosestMetaGraph(final CompositeTableGraph root, final CompositeTable table, final int level,
            final int maxLevel) {
        CompositeTableGraph result = root;

        if (level > maxLevel) {
            return result;
        }

        double minDist = Double.MAX_VALUE;
        for (final CompositeTableGraph child : root.children()) {
            if (!(child.getTable() instanceof MetaTable)) {
                continue;
            }

            final CompositeTableGraph grandChild = findClosestMetaGraph(child, table, level + 1, maxLevel);
            final double dist1 = distanceBetweenTables(grandChild.getTable(), table);
            final double dist2 = distanceBetweenTables(child.getTable(), table);

            if (dist1 < dist2) {
                if (dist1 < minDist) {
                    minDist = dist1;
                    result = grandChild;
                }
            } else {
                if (dist2 < minDist) {
                    minDist = dist2;
                    result = child;
                }
            }
        }

        return result;
    }

    private double distanceBetweenTables(final CompositeTable table1, final CompositeTable table2) {
        final int vx = table2.getFirstColumn() - table1.getFirstColumn();
        final int vy = table2.getFirstRow() - table1.getLastRow() - 1;
        if (vx >= 0 && vy >= 0) {
            return vx + vy;
        } else {
            return Double.MAX_VALUE;
        }
    }

    private final SheetParser sheetParser;
}
