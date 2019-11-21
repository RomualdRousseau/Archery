package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.ITable;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable implements ITable {

    public IntelliTable(TableGraph root) {
        this.walkThroughTableGraph(root, 0, 0);
    }

    @Override
    public int getNumberOfColumns() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfRows() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterable<IRow> rows() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfHeaders() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Iterable<IHeader> headers() {
        // TODO Auto-generated method stub
        return null;
    }

    private int walkThroughTableGraph(TableGraph graph, int indent, int counter) {
        if (!graph.isRoot()) { // && graph.getTable() instanceof DataTable) {
            for (int i = 0; i < indent; i++) {
                System.out.print("|- ");
            }

            for (IHeader header : graph.getTable().headers()) {
                System.out.print(header.getName() + " ");
            }

            System.out.println(graph.getTable().getFirstColumn() + " "
                    + graph.getTable().getFirstRow() + " " + graph.getTable().getLastColumn() + " "
                    + graph.getTable().getLastRow() + " " + graph.getTable().getNumberOfRows() + " ");

            if(graph.getTable() instanceof DataTable) {
                System.out.println(counter + 1);
                counter++;
            } else {
                System.out.println();
            }
        }

        for (TableGraph child : graph.children()) {
            counter = walkThroughTableGraph(child, indent + 1, counter);
        }

        return counter;
    }
}
