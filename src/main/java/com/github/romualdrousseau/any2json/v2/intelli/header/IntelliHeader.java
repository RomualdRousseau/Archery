package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.base.AbstractHeader;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.any2json.v2.intelli.IntelliTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;
import com.github.romualdrousseau.shuju.util.StringUtility;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.Header;
import com.github.romualdrousseau.any2json.v2.HeaderTag;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;

public class IntelliHeader extends AbstractHeader {

    public IntelliHeader(final IntelliTable table, final BaseCell cell) {
        super(table, cell);
        assert(getTable().getClassifier() != null) : "Classifier must be defined";
    }

    public IntelliHeader(final AbstractHeader header) {
        super(header.getTable(), new BaseCell(header.getName(), 0, 1, header.getTable().getClassifier()));
        assert(getTable().getClassifier() != null) : "Classifier must be defined";
        this.setColumnIndex(header.getColumnIndex());
    }

    private IntelliHeader(final IntelliHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public Vector getEntityVector() {
        if (this.entityVector == null) {
            this.entityVector = this.buildEntityVector();
            if(this.entityVector == null) {
                this.entityVector = new Vector(this.getTable().getClassifier().getWordList().getVectorSize());
            }
        }
        return this.entityVector;
    }

    @Override
    public BaseCell getCellForRow(final BaseRow row) {
        return this.getCell();
    }

    @Override
    public String getCellMergedValue(final BaseRow row) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(this.getCellForRow(row).getValue());

        IntelliHeader curr = this;
        while (curr.nextSibbling != null) {
            buffer.append(this.getCellForRow(row).getValue());
            curr = curr.nextSibbling;
        }

        return buffer.toString();
    }

    @Override
    public AbstractHeader clone() {
        return new IntelliHeader(this);
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
    public DataRow buildTrainingRow(final String tagValue, final Header[] conflicts, final boolean ensureWordsExists) {
        if (ensureWordsExists) {
            this.ensureWordExist();
            this.wordVector = null;
            if (conflicts != null) {
                for (final Header conflict : conflicts) {
                    ((IntelliHeader) conflict).ensureWordExist();
                    ((IntelliHeader) conflict).wordVector = null;
                }
            }
        }

        Vector label = this.getTable().getClassifier().getTagList().word2vec(tagValue);
        return new DataRow().addFeature(this.buildFeature(true)).setLabel(label);
    }

    @Override
    public IntelliTable getTable() {
        return (IntelliTable) super.getTable();
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
            final String tagValue = this.getTable().getClassifier().predict(data);
            this.tag = new HeaderTag(tagValue);
        }
    }

    public void mergeTo(final IntelliHeader other) {
        this.nextSibbling = other;
    }

    private Vector getWordVector() {
        if (this.wordVector == null) {
            this.wordVector = this.buildWordVector();
            if(this.wordVector == null) {
                this.wordVector = new Vector(this.getTable().getClassifier().getWordList().getVectorSize());
            }
        }
        return this.wordVector;
    }

    private Vector getConflictVector(boolean checkForConflicts) {
        final Vector result = new Vector(this.getTable().getClassifier().getWordList().getVectorSize());

        if(!checkForConflicts) {
            return result;
        }

        AbstractHeader[] conflicts = this.findConflictingHeaders();
        if (conflicts == null) {
            return result;
        }

        for (final Header conflict : conflicts) {
            result.add(((IntelliHeader) conflict).getWordVector());
        }

        return result.constrain(0, 1);
    }

    private Vector buildEntityVector() {
        final Vector result = new Vector(this.getTable().getClassifier().getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(),
                this.getTable().getClassifier().getSampleCount()); i++) {
            final BaseRow row = this.getTable().getRowAt(i);
            if (row == null) {
                continue;
            }

            final BaseCell cell = row.getCellAt(this.getColumnIndex());
            if (cell.hasValue() && !cell.getEntityVector().isNull()) {
                result.add(cell.getEntityVector());
                n++;
            }
        }

        if (n > 0) {
            result.cond(DocumentFactory.DEFAULT_ENTITY_PROBABILITY * ((float) n), 0.0f, 1.0f);
        }

        return result;
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

    private String name;
    private Vector entityVector;
    private Vector wordVector;
    private HeaderTag tag;
    private IntelliHeader nextSibbling;
}
