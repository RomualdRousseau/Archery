package com.github.romualdrousseau.any2json.intelli.header;

import java.util.List;
import java.util.stream.IntStream;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.HeaderTag;
import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;
import com.github.romualdrousseau.shuju.math.Tensor;
import com.github.romualdrousseau.shuju.util.StringUtils;

public class IntelliHeader extends CompositeHeader {

    public IntelliHeader(final CompositeHeader header) {
        this(header.getTable(), new BaseCell(header.getName(), header.getColumnIndex(), 1, header.getCell().getRawValue(), header.getClassifierFactory()), header instanceof MetaHeader);
        this.setColumnIndex(header.getColumnIndex());
    }

    private IntelliHeader(final CompositeTable table, final BaseCell cell, boolean isMeta) {
        super(table, cell);
        this.isMeta = isMeta;
        this.entities = this.sampleEntities();

        final String cellValue = this.getCell().getValue();
        if(StringUtils.isFastBlank(cellValue)) {
            this.name = this.entities().stream().findAny().map(x -> this.getEntitiesAsString()).orElse(DocumentFactory.PIVOT_VALUE_SUFFIX);
        } else if (isMeta) {
            this.name = this.entities().stream().findAny().map(x -> this.getEntitiesAsString()).orElse(cellValue);
        } else {
            this.name = this.getLayoutClassifier().toEntityName(cellValue);
        }
    }

    @Override
    public String getName() {
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
    public List<String> entities() {
        return this.entities;
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
        if (StringUtils.isFastBlank(this.getName())) {
            this.tag = HeaderTag.None;
        } else {
            this.getClassifierFactory().getTagClassifier().ifPresent(classifier -> { 
                final String tagValue = classifier.predict(classifier.buildPredictSet(this.getName(), this.entities(), this.getTable().getHeaderNames()));
                this.tag = new HeaderTag(tagValue);
            });
        }
    }

    public void mergeTo(final IntelliHeader other) {
        this.nextSibbling = other;
    }

    private List<String> sampleEntities() {
        final int N = Math.min(this.getTable().getNumberOfRows(), this.getLayoutClassifier().getSampleCount());
        final Tensor entityVector = Tensor.zeros(this.getLayoutClassifier().getEntityList().size());
        float n = 0.0f;
        for (int i = 0; i < N; i++) {
            final BaseRow row = this.getTable().getRowAt(i);
            if (row == null) {
                continue;
            }
            final BaseCell cell = row.getCellAt(this.getColumnIndex());
            if (cell.hasValue() && cell.getSymbol().equals("e")) {
                entityVector.iadd(cell.getEntityVector());
                n += DocumentFactory.DEFAULT_ENTITY_PROBABILITY;
            }
        }
        if (n > 0.0f) {
            entityVector.if_lt_then(n, 0.0f, 1.0f);
        }
        final List<String> entityList = this.getLayoutClassifier().getEntityList();
        return IntStream.range(0, entityVector.size).boxed().filter(i -> entityVector.data[i] == 1).map(i -> entityList.get(i)).toList();
    }

    private final String name;
    private final boolean isMeta;
    private final List<String> entities;
    private HeaderTag tag;
    private IntelliHeader nextSibbling;
}
