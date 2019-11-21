package com.github.romualdrousseau.any2json.v2.util;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.github.romualdrousseau.any2json.v2.base.Table;

public class TableGraph {

    public TableGraph() {
        this.table = null;
        this.parent = null;
    }

    public TableGraph(Table table) {
        this.table = table;
        this.parent = null;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public Table getTable() {
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

    private Table table;
    private TableGraph parent;
    private LinkedList<TableGraph> children = new LinkedList<TableGraph>();
}
