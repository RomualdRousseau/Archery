package com.github.romualdrousseau.any2json.intelli.event;

import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.intelli.DataTable;
import com.github.romualdrousseau.any2json.util.TableGraph;

public class TableGraphBuiltEvent extends SheetEvent {

    public TableGraphBuiltEvent(final Sheet source, final TableGraph tableGraph) {
        super(source);
        this.tableGraph = tableGraph;
    }

    public TableGraph getTableGraph() {
        return this.tableGraph;
    }

    public void dumpTableGraph() {
        System.out.println("============================ DUMP TABLEGRAPH ============================");
        System.out.println(this.getSource().getName());
        this.walkThroughTableGraph(this.tableGraph, 0, 0);
        System.out.println("================================== END ==================================");
    }

    private int walkThroughTableGraph(final TableGraph graph, final int indent, int counter) {
        if (!graph.isRoot()) {
            final StringBuffer out = new StringBuffer();

            for (int i = 0; i < indent; i++) {
                out.append("|- ");
            }

            for (final Header header : graph.getTable().headers()) {
                out.append(header.getName()).append(" ");
            }

            if (graph.getTable() instanceof DataTable) {
                out.append("DATA(");
            } else {
                out.append("META(");
            }
            out.append(graph.getTable().getFirstColumn()).append(", ");
            out.append(graph.getTable().getFirstRow()).append(", ");
            out.append(graph.getTable().getLastColumn()).append(", ");
            out.append(graph.getTable().getLastRow()).append(", ");
            out.append(graph.getTable().getLastRow() - graph.getTable().getFirstRow() + 1).append(", ");
            out.append(graph.getTable().getNumberOfRows());
            out.append(")");

            if (graph.getTable() instanceof DataTable) {
                out.append(" (").append(counter + 1).append(")");
                counter++;
            }

            System.out.println(out.toString());
        }

        for (final TableGraph child : graph.children()) {
            counter = walkThroughTableGraph(child, indent + 1, counter);
        }

        return counter;
    }

    private final TableGraph tableGraph;
}
