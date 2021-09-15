package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.shuju.math.Tensor1D;

public class PivotValueHeader extends PivotKeyHeader {

    public PivotValueHeader(final PivotKeyHeader parent) {
        super(parent.getTable(), parent.getCell());
        this.name = parent.getPivotEntityString();
    }

    @Override
    public String getName() {
        if(!this.getTable().isLoadCompleted()) {
            return DocumentFactory.PIVOT_VALUE_SUFFIX;
        } else {
            return this.name + " " + DocumentFactory.PIVOT_VALUE_SUFFIX;
        }
    }

    @Override
    public PivotValueHeader clone() {
        return new PivotValueHeader(this);
    }

    public Tensor1D buildEntityVector() {
        final Tensor1D result = new Tensor1D(this.getTable().getSheet().getClassifierFactory().getLayoutClassifier().get().getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(),
            this.getTable().getSheet().getClassifierFactory().getLayoutClassifier().get().getSampleCount()); i++) {
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
}
