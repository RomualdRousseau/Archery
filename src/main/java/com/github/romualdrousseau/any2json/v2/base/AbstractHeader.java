package com.github.romualdrousseau.any2json.v2.base;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

public abstract class AbstractHeader implements Header {

    public abstract AbstractCell getCell(AbstractRow row);

    public abstract AbstractHeader clone();

    protected abstract AbstractHeader[] findConflictingHeaders();

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

    public int getColumnIndex() {
        return this.colIndex;
    }

    public void setColumnIndex(final int colIndex) {
        this.colIndex = colIndex;
    }

    public AbstractTable getTable() {
        return this.table;
    }

    public AbstractCell getCell() {
        return this.cell;
    }

    public String getCellValue(final AbstractRow row, final boolean merged) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(this.getCell(row).getValue());

        AbstractHeader curr = this;
        while(merged && curr.nextSibbling != null) {
            buffer.append(this.getCell(row).getValue());
            curr = curr.nextSibbling;
        }

		return buffer.toString();
	}

    public void chainTo(final AbstractHeader other) {
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

    public DataRow buildTrainingRow(final String tagValue, final AbstractHeader[] conflicts,
            final boolean ensureWordsExists) {
        if (ensureWordsExists) {
            this.ensureWordExist();
            this.wordVector = null;
            if (conflicts != null) {
                for (final AbstractHeader conflict : conflicts) {
                    conflict.ensureWordExist();
                    conflict.wordVector = null;
                }
            }
        }
        return new DataRow().addFeature(this.buildFeature(conflicts))
                .setLabel(this.table.getClassifier().getTagList().word2vec(tagValue));
    }

    public boolean equals(final AbstractHeader o) {
        return this.getName().equalsIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof AbstractHeader && this.equals((AbstractHeader) o);
    }

    protected Vector getEntityVector() {
        if (this.table.getClassifier() != null && this.entityVector == null) {
            this.entityVector = this.table.getClassifier().getEntityList().word2vec(this.getName());
        } else {
            this.entityVector = new Vector(this.table.getClassifier().getEntityList().getVectorSize());
        }
        return this.entityVector;
    }

    protected Vector getWordVector() {
        if (this.table.getClassifier() != null && this.wordVector == null) {
            this.wordVector = this.table.getClassifier().getWordList().word2vec(this.getName());
        } else {
            this.wordVector = new Vector(this.table.getClassifier().getWordList().getVectorSize());
        }
        return this.wordVector;
    }

    protected void ensureWordExist() {
        this.getTable().getClassifier().getWordList().add(this.getName());
    }

    protected Vector getConflictVector(final boolean checkForConflicts) {
        final AbstractHeader[] conflicts = checkForConflicts ? this.findConflictingHeaders() : null;
        return this.buildConflitVector(conflicts);
    }

    protected AbstractHeader[] findConflictingHeaders(final AbstractHeader header, final HeaderTag tag,
            final Iterable<Header> others) {
        final ArrayList<AbstractHeader> result = new ArrayList<AbstractHeader>();

        for (final Header other : others) {
            if (other != header && other.hasTag() && !other.getTag().isUndefined() && other.getTag().equals(tag)) {
                result.add((AbstractHeader) other);
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            return result.toArray(new AbstractHeader[result.size()]);
        }
    }

    private Vector buildFeature(final boolean checkForConflicts) {
        final Vector entity2vec = this.getEntityVector();
        final Vector word2vec = this.getWordVector();
        final Vector conflict2vec = this.getConflictVector(checkForConflicts);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private Vector buildFeature(final AbstractHeader[] conflicts) {
        final Vector entity2vec = this.getEntityVector();
        final Vector word2vec = this.getWordVector();
        final Vector conflict2vec = this.buildConflitVector(conflicts);
        return entity2vec.concat(word2vec).concat(conflict2vec);
    }

    private Vector buildConflitVector(final AbstractHeader[] conflicts) {
        final Vector result = new Vector(this.table.getClassifier().getWordList().getVectorSize());

        if (conflicts == null) {
            return result;
        }

        for (final AbstractHeader conflict : conflicts) {
            result.add(conflict.getWordVector());
        }
        return result.constrain(0, 1);
    }

    private final AbstractTable table;
    private final AbstractCell cell;
    private int colIndex;
    private AbstractHeader nextSibbling;
    private HeaderTag tag;
    private Vector entityVector;
    private Vector wordVector;
}
