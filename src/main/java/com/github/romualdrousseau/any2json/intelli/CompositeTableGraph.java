package com.github.romualdrousseau.any2json.intelli;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CompositeTableGraph {

    public CompositeTableGraph() {
        this.table = null;
        this.parent = null;
    }

    public CompositeTableGraph(final CompositeTable table) {
        this.table = table;
        this.parent = null;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public CompositeTable getTable() {
        return this.table;
    }

    public CompositeTableGraph getParent() {
        return this.parent;
    }

    public List<CompositeTableGraph> children() {
        return this.children;
    }

    public void addChild(final CompositeTableGraph child) {
        child.parent = this;
        this.children.add(child);

        this.children.sort(new Comparator<CompositeTableGraph>() {
            @Override
            public int compare(final CompositeTableGraph o1, final CompositeTableGraph o2) {
                return o1.table.getFirstRow() - o2.table.getFirstRow();
            }
        });
    }

    private final CompositeTable table;
    private CompositeTableGraph parent;
    private final LinkedList<CompositeTableGraph> children = new LinkedList<CompositeTableGraph>();
}
