package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.v2.IHeader;
import com.github.romualdrousseau.any2json.v2.IRow;
import com.github.romualdrousseau.any2json.v2.ITable;
import com.github.romualdrousseau.any2json.v2.base.Header;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable implements ITable {

    public IntelliTable(TableGraph root) {
        this.walkThroughTableGraph(root, 0, 0);

        this.buildHeaders(root);

        for(IHeader header : this.headers) {
            System.out.println(header.getName() + ": " + header.getValue());
        }
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
        return this.headers;
    }

    private void buildHeaders(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            for (IHeader header : child.getTable().headers()) {
                if(header instanceof PivotHeader) {
                    PivotHeader pivotHeader = (PivotHeader) header;

                    Header newHeader = pivotHeader.clone();
                    newHeader.setColumnIndex(this.headers.size());
                    this.headers.addLast(newHeader);

                    newHeader = pivotHeader.getValueHeader();
                    newHeader.setColumnIndex(this.headers.size());
                    this.headers.addLast(newHeader);
                }
                else if(!this.headers.contains(header)) {
                    Header newHeader = ((Header) header).clone();
                    newHeader.setColumnIndex(this.headers.size());
                    this.headers.addLast(newHeader);
                }
            }
        }

        for (TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private int walkThroughTableGraph(TableGraph graph, int indent, int counter) {
        if (!graph.isRoot()) { // && graph.getTable() instanceof DataTable) {
            StringBuffer out = new StringBuffer();

            for (int i = 0; i < indent; i++) {
                out.append("|- ");
            }

            for (IHeader header : graph.getTable().headers()) {
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

            if(graph.getTable() instanceof DataTable) {
                out.append(" (").append(counter + 1).append(")");
                counter++;
            }

            System.out.println(out.toString());
        }

        for (TableGraph child : graph.children()) {
            counter = walkThroughTableGraph(child, indent + 1, counter);
        }

        return counter;
    }

    private LinkedList<IHeader> headers = new LinkedList<IHeader>();
}
