package com.github.romualdrousseau.any2json.v2.intelli;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable extends AbstractTable {

    public IntelliTable(TableGraph root, ITagClassifier classifier) {
        super(classifier);

        this.buildHeaders(root);

        this.buildRows(root);
    }

    private void buildHeaders(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            for (Header header : child.getTable().headers()) {
                if (header instanceof PivotHeader) {
                    PivotHeader pivotHeader = (PivotHeader) header;

                    AbstractHeader newHeader = pivotHeader.clone();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);

                    newHeader = pivotHeader.getValueHeader();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                } else if (!this.checkIfHeaderExists((AbstractHeader) header)) {
                    AbstractHeader newHeader = ((AbstractHeader) header).clone();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                }
            }
        }

        for (TableGraph child : graph.children()) {
            buildHeaders(child);
        }
    }

    private void buildRows(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            if(child.getTable() instanceof DataTable) {


            }
        }

        for (TableGraph child : graph.children()) {
            buildRows(child);
        }
    }
}
