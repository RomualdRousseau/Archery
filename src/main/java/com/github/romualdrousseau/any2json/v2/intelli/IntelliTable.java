package com.github.romualdrousseau.any2json.v2.intelli;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.ITagClassifier;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.Row;
import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.AbstractRow;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;
import com.github.romualdrousseau.any2json.v2.intelli.header.MetaHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.PivotHeader;
import com.github.romualdrousseau.any2json.v2.intelli.header.TaggedHeader;
import com.github.romualdrousseau.any2json.v2.util.TableGraph;

public class IntelliTable extends AbstractTable {

    public IntelliTable(TableGraph root, ITagClassifier classifier) {
        super(classifier);
        this.buildHeaders(root);
        this.buildRows(root);
    }

    @Override
    public int getNumberOfColumns() {
        return this.getNumberOfHeaders();
    }

    @Override
    public int getNumberOfRows() {
        return this.rows.size();
    }

    @Override
    public AbstractRow getRowAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumberOfRows()) {
            throw new ArrayIndexOutOfBoundsException(rowIndex);
        }

        return this.rows.get(rowIndex);
    }

    private void buildHeaders(TableGraph graph) {
        for (TableGraph child : graph.children()) {
            for (Header header : child.getTable().headers()) {
                if (this.checkIfHeaderExists((AbstractHeader) header)) {
                    continue;
                }

                if (header instanceof PivotHeader) {
                    PivotHeader pivotHeader = (PivotHeader) header;
                    AbstractHeader newHeader = pivotHeader.clone();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);

                    newHeader = pivotHeader.getValueHeader();
                    newHeader.setColumnIndex(this.getNumberOfHeaders());
                    this.addHeader(newHeader);
                } else  {
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
            if (child.getTable() instanceof DataTable) {
                this.buildRowsForOneTable((DataTable) child.getTable());
            }
        }

        for (TableGraph child : graph.children()) {
            buildRows(child);
        }
    }

    private void buildRowsForOneTable(DataTable orgTable) {
        for (Row orgRow : orgTable.rows()) {
            ArrayList<IntelliRow> newRows = buildFromOneRow(orgTable, (AbstractRow) orgRow);
            this.rows.addAll(newRows);
        }
    }

    private ArrayList<IntelliRow> buildFromOneRow(DataTable orgTable, AbstractRow orgRow) {
        ArrayList<IntelliRow> newRows = new ArrayList<IntelliRow>();

        IntelliRow newRow = new IntelliRow(this);

        for (Header header : this.headers()) {
            if (header instanceof PivotHeader) {
                PivotHeader pivotHeader = (PivotHeader) header;
                if (!pivotHeader.isPivotalKey()) {
                    continue;
                }

                boolean firstPass = true;
                PivotHeader orgHeader = (PivotHeader) orgTable.findHeader((AbstractHeader) header);
                if(orgHeader != null) {
                    for (int i : orgHeader.getColumnIndexes()) {
                        if(firstPass) {
                            firstPass = false;
                        } else {
                            IntelliRow prevRow = newRow.clone();
                            newRows.add(newRow);
                            newRow = prevRow;
                        }
                        newRow.addCell(orgTable.getRowAt(orgTable.getHeaderRowOffset()).getCellAt(i));
                        newRow.addCell(orgRow.getCellAt(i));
                    }
                } else {
                    newRow.addEmptyCell();
                    newRow.addEmptyCell();
                }
            } else if (header instanceof MetaHeader) {
                AbstractHeader orgHeader = orgTable.findHeader((AbstractHeader) header);
                if(orgHeader != null) {
                    newRow.addStringCell(orgHeader.getValue());
                } else {
                    newRow.addStringCell(header.getValue());
                }
            } else if (header instanceof TaggedHeader) {
                AbstractHeader orgHeader = orgTable.findHeader((AbstractHeader) header);
                if(orgHeader != null) {
                    newRow.addCell(orgRow.getCellAt(orgHeader.getColumnIndex()));
                } else {
                    newRow.addEmptyCell();
                }
            }
        }

        newRows.add(newRow);

        return newRows;
    }

    private ArrayList<IntelliRow> rows = new ArrayList<IntelliRow>();
}
