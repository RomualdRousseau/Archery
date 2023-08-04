package com.github.romualdrousseau.any2json.intelli;

import java.util.List;

import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.base.SheetStore;
import com.github.romualdrousseau.any2json.util.Visitable;

public class IntelliSheet extends TransformableSheet {

    public IntelliSheet(SheetStore store, IntelliSheetParser parser) {
        super(store);
        this.sheetParser = parser;
    }

    @Override
    public Table parseAllTables() {
        final CompositeTable table = this.sheetParser.parseAllTables(this);
        table.prepareHeaders();
        table.updateHeaderTags();
        table.setLoadCompleted(true);
        return table;
    }

    public CompositeTableGraph buildTableGraph(final List<MetaTable> metaTables, final List<DataTable> dataTables) {
        final CompositeTableGraph root = new CompositeTableGraph();

        for (final Visitable e : metaTables) {
            e.setVisited(false);
        }

        // First attach all not snapped metaTables to the root nodes
        for (final MetaTable metaTable : metaTables) {
            if (!this.isJoint(metaTable, dataTables)) {
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

    private final IntelliSheetParser sheetParser;
}
