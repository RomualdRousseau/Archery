package com.github.romualdrousseau.any2json.v2.base;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

public abstract class AbstractHeader implements Header {

    public abstract AbstractCell getCellForRow(AbstractRow row);

    public abstract AbstractHeader clone();

    public AbstractHeader(final AbstractTable table, final AbstractCell cell) {
        this.table = table;
        this.cell = cell;
        this.colIndex = cell.getColumnIndex();
    }

    @Override
    public boolean hasTag() {
        return this.tag != null;
    }

    @Override
    public HeaderTag getTag() {
        return this.tag;
    }

    @Override
    public Vector getEntityVector() {
        if (this.entityVector == null) {
            this.entityVector = this.buildEntityVector();
        }
        return this.entityVector;
    }

    public Vector getWordVector() {
        if (this.wordVector == null) {
            this.wordVector = this.buildWordVector();
            if(this.wordVector == null) {
                this.wordVector = new Vector(this.table.getClassifier().getWordList().getVectorSize());
            }
        }
        return this.wordVector;
    }

    public Vector getConflictVector(boolean checkForConflicts) {
        final Vector result = new Vector(this.table.getClassifier().getWordList().getVectorSize());

        if(!checkForConflicts) {
            return result;
        }

        AbstractHeader[] conflicts = this.findConflictingHeaders();
        if (conflicts == null) {
            return result;
        }

        for (final Header conflict : conflicts) {
            result.add(((AbstractHeader) conflict).getWordVector());
        }

        return result.constrain(0, 1);
    }

    @Override
    public DataRow buildTrainingRow(final String tagValue, final Header[] conflicts, final boolean ensureWordsExists) {
        if (ensureWordsExists) {
            this.ensureWordExist();
            this.wordVector = null;
            if (conflicts != null) {
                for (final Header conflict : conflicts) {
                    ((AbstractHeader) conflict).ensureWordExist();
                    ((AbstractHeader) conflict).wordVector = null;
                }
            }
        }

        Vector label = this.table.getClassifier().getTagList().word2vec(tagValue);
        return new DataRow().addFeature(this.buildFeature(true)).setLabel(label);
    }

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(final int colIndex) {
        this.colIndex = colIndex;
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public void setTable(final IntelliTable table) {
        this.table = table;
    }

    public AbstractCell getCell() {
        return this.cell;
    }

    public String getCellMergedValue(final AbstractRow row) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(this.getCellForRow(row).getValue());

        AbstractHeader curr = this;
        while (curr.nextSibbling != null) {
            buffer.append(this.getCellForRow(row).getValue());
            curr = curr.nextSibbling;
        }

        return buffer.toString();
    }

    public void mergeTo(final AbstractHeader other) {
        this.nextSibbling = other;
    }

    public void resetTag() {
        this.wordVector = null;
        this.entityVector = null;
        this.tag = null;
        this.nextSibbling = null;
    }

    public void updateTag(final boolean checkForConflicts) {
        if (StringUtility.isEmpty(this.getName())) {
            this.tag = HeaderTag.None;
        } else {
            final DataRow data = new DataRow().addFeature(this.buildFeature(checkForConflicts));
            final String tagValue = this.table.getClassifier().predict(data);
            this.tag = new HeaderTag(tagValue);
        }
    }

    public boolean equals(final AbstractHeader o) {
        return this.getName().equalsIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    protected Vector buildEntityVector() {
        return this.getCell().getEntityVector();
    }

    protected Vector buildWordVector() {
        return this.getTable().getClassifier().getWordList().word2vec(this.getName());
    }

    protected void ensureWordExist() {
        this.getTable().getClassifier().getWordList().add(this.getName());
    }

    private Vector buildFeature(boolean checkForConflicts) {
        final Vector entity2vec = this.getEntityVector();
        final Vector word2vec = this.getWordVector();
        final Vector conflict2vec = this.getConflictVector(checkForConflicts);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private AbstractHeader[] findConflictingHeaders() {
        final ArrayList<AbstractHeader> result = new ArrayList<AbstractHeader>();

        for (final Header other : this.getTable().headers()) {
            if (other != this && other.hasTag() && !other.getTag().isUndefined() && other.getTag().equals(this.getTag())) {
                result.add((AbstractHeader) other);
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            return result.toArray(new AbstractHeader[result.size()]);
        }
    }

    private AbstractTable table;
    private final AbstractCell cell;
    private int colIndex;
    private AbstractHeader nextSibbling;
    private HeaderTag tag;
    private Vector entityVector;
    private Vector wordVector;
}
