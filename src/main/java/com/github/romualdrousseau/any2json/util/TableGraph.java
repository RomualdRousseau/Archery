package com.github.romualdrousseau.any2json.util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class TableGraph {

    public TableGraph() {
        this.table = null;
        this.parent = null;
    }

    public TableGraph(CompositeTable table) {
        this.table = table;
        this.parent = null;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public CompositeTable getTable() {
        return this.table;
    }

    public TableGraph getParent() {
        return this.parent;
    }

    public List<TableGraph> children() {
        return this.children;
    }

    public void addChild(TableGraph child) {
        child.parent = this;
        this.children.add(child);

        this.children.sort(new Comparator<TableGraph>() {
			@Override
			public int compare(TableGraph o1, TableGraph o2) {
				return o1.table.getFirstRow() - o2.table.getFirstRow();
			}
        });
    }

    private CompositeTable table;
    private TableGraph parent;
    private LinkedList<TableGraph> children = new LinkedList<TableGraph>();
}
