package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Tensor1D;
import com.github.romualdrousseau.shuju.nlp.RegexList;
import com.github.romualdrousseau.shuju.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseRow;

public class IntelliHeader extends CompositeHeader {

    public IntelliHeader(final CompositeTable table, final BaseCell cell) {
        super(table, cell);
        this.isMeta = false;
    }

    public IntelliHeader(final CompositeHeader header) {
        super(header.getTable(), new BaseCell(header.getName(), header.getColumnIndex(), 1, header.getRawName(), header.getTable().getSheet().getClassifierFactory()));
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
            this.name = this.getLayoutClassifier().getStopWordList().removeStopWords(v1);
            if(StringUtility.isFastEmpty(this.name)) {
                final Tensor1D v = this.sampleEntityVector();
                if(v.sparsity() < 1.0f) {
                    this.name = this.getLayoutClassifier().getEntityList().get(v.argmax());
                } else {
                    this.name =  DocumentFactory.PIVOT_VALUE_SUFFIX;
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
            return new BaseCell(buffer, this.getColumnIndex(), 1, this.getClassifierFactory());
        }
    }

    @Override
    public Iterable<String> entities() {
        final List<String> result = new ArrayList<String>();
        final Tensor1D entityVector = this.sampleEntityVector();
        final RegexList entityList = this.getLayoutClassifier().getEntityList();
        for (int i = 0; i < entityList.size(); i++) {
            if (entityVector.get(i) == 1) {
                result.add(entityList.get(i));
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
    public CompositeTable getTable() {
        return (CompositeTable) super.getTable();
    }

    public void resetTag() {
        this.tag = null;
        this.nextSibbling = null;
    }

    public void updateTag() {
        if (StringUtility.isFastEmpty(this.getName())) {
            this.tag = HeaderTag.None;
        } else {
            this.getClassifierFactory().getTagClassifier().ifPresent(classifier -> {
                final DataRow data = classifier.buildPredictRow(this.getName(), this.entities(), this.getTable().getHeaderNames());
                final String tagValue = classifier.predict(data);
                this.tag = new HeaderTag(tagValue);
            });
        }
    }

    public void mergeTo(final IntelliHeader other) {
        this.nextSibbling = other;
    }

    private Tensor1D sampleEntityVector() {
        final Tensor1D result = new Tensor1D(this.getLayoutClassifier().getEntityList().getVectorSize());
        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(), this.getLayoutClassifier().getSampleCount()); i++) {
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

    private String name;
    private HeaderTag tag;
    private IntelliHeader nextSibbling;
    private final boolean isMeta;
}
