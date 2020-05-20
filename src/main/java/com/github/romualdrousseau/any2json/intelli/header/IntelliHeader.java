package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.util.StringUtility;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;

public class IntelliHeader extends CompositeHeader {

    public IntelliHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
        this.isMeta = false;
    }

    public IntelliHeader(final CompositeHeader header) {
        super(header.getTable(), new BaseCell(header.getName(), header.getColumnIndex(), 1, header.getTable().getClassifier()));
        this.setColumnIndex(header.getColumnIndex());
        this.isMeta = header instanceof MetaHeader;
    }

    private IntelliHeader(final IntelliHeader parent) {
        this(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if (this.name == null) {
            final String v1 = this.getCell().getValue();
            this.name = this.getTable().getClassifier().getStopWordList().removeStopWords(v1);
            if(this.name.isEmpty()) {
                final Tensor1D v = this.getEntityVector();
                if(v.sparsity() < 1.0f) {
                    this.name = this.getTable().getClassifier().getEntityList().get(v.argmax());
                } else {
                    this.name = "#VALUE?";
                }
            }
        }
        return this.name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public BaseCell getCellAtRow(final Row row, final boolean merged) {
        if (!merged || this.nextSibbling == null) {
            return this.getCellAtRow(row);
        }

        String buffer = "";

        IntelliHeader curr = this;
        while (curr != null) {
            final String value = curr.getCellAtRow(row).getValue();
            if (!curr.isMeta && !buffer.contains(value)) {
                buffer += value;
            }
            curr = curr.nextSibbling;
        }

        if (buffer.isEmpty()) {
            return this.getCellAtRow(row);
        } else {
            return new BaseCell(buffer, this.getColumnIndex(), 1, this.getTable().getClassifier());
        }
    }

    @Override
    public String getEntityString() {
        String result = "";
        boolean firstValue = true;
        for (int i = 0; i < this.getTable().getClassifier().getEntityList().size(); i++) {
            if (this.getEntityVector().get(i) == 1) {
                if (firstValue) {
                    result = this.getTable().getClassifier().getEntityList().get(i);
                    firstValue = false;
                } else {
                    result += "|" + this.getTable().getClassifier().getEntityList().get(i);
                }
            }
        }
        return result;
    }

    @Override
    public CompositeHeader clone() {
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
    public DataRow buildTrainingRow(final String tagValue, final boolean ensureWordsExists) {
        if (ensureWordsExists) {
            for (final Header other : this.getTable().headers()) {
                ((IntelliHeader) other).ensureWordExist();
            }
        }

        final Tensor1D label = this.getTable().getClassifier().getTagList().word2vec(tagValue);
        return new DataRow().addFeature(this.getEntityVector()).addFeature(this.getWordVector())
                .addFeature(this.getContextVector()).setLabel(label);
    }

    @Override
    public CompositeTable getTable() {
        return (CompositeTable) super.getTable();
    }

    public void resetTag() {
        this.entityVector = null;
        this.wordVector = null;
        this.contextVector = null;
        this.tag = null;
        this.nextSibbling = null;
    }

    public void updateTag() {
        if (StringUtility.isFastEmpty(this.getName())) {
            this.tag = HeaderTag.None;
        } else {
            final DataRow data = new DataRow().addFeature(this.getEntityVector()).addFeature(this.getWordVector())
                    .addFeature(this.getContextVector());
            final String tagValue = this.getTable().getClassifier().predict(data);
            this.tag = new HeaderTag(tagValue);
        }
    }

    public void mergeTo(final IntelliHeader other) {
        this.nextSibbling = other;
    }

    private Tensor1D getEntityVector() {
        if (this.entityVector == null) {
            this.entityVector = this.buildEntityVector();
        }
        return this.entityVector;
    }

    private Tensor1D getWordVector() {
        if (this.wordVector == null) {
            this.wordVector = this.buildWordVector();
        }
        return this.wordVector;
    }

    private Tensor1D getContextVector() {
        if (this.contextVector == null) {
            this.contextVector = this.buildContextVector();
        }
        return this.contextVector;
    }

    private Tensor1D buildEntityVector() {
        final Tensor1D result = new Tensor1D(this.getTable().getClassifier().getEntityList().getVectorSize());

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
            result.if_lt_then(DocumentFactory.DEFAULT_ENTITY_PROBABILITY * ((float) n), 0.0f, 1.0f);
        }

        return result;
    }

    private Tensor1D buildWordVector() {
        return this.getTable().getClassifier().getWordList().word2vec(this.getName());
    }

    private Tensor1D buildContextVector() {
        final Tensor1D result = this.getWordVector().copy().zero();

        final Iterable<Header> context = this.getTable().headers();
        if (context == null) {
            return result;
        }

        for (final Header other : context) {
            result.add(((IntelliHeader) other).getWordVector());
        }

        final Tensor1D word_mask = this.getWordVector().copy().ones().sub(this.getWordVector());
        return result.mul(word_mask).constrain(0, 1);
    }

    private void ensureWordExist() {
        this.getTable().getClassifier().getWordList().add(this.getName());
        this.entityVector = null;
        this.wordVector = null;
        this.contextVector = null;
    }

    private String name;
    private Tensor1D entityVector;
    private Tensor1D wordVector;
    private Tensor1D contextVector;
    private HeaderTag tag;
    private IntelliHeader nextSibbling;
    private final boolean isMeta;
}
