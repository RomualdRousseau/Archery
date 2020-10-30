package com.github.romualdrousseau.any2json.intelli.header;

import com.github.romualdrousseau.any2json.ClassifierFactory;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseRow;
import com.github.romualdrousseau.shuju.math.Tensor1D;

public class PivotValueHeader extends PivotKeyHeader {

    public PivotValueHeader(final PivotKeyHeader parent) {
        super(parent.getTable(), parent.getCell());
    }

    @Override
    public String getName() {
        if(!this.getTable().isLoadCompleted()) {
            return "#VALUE? " + DocumentFactory.PIVOT_SUFFIX;
        }

        if (this.name == null) {
            final Tensor1D v = this.buildEntityVector();
            if(v.sparsity() < 1.0f) {
                this.name = ClassifierFactory.get().getLayoutClassifier().get().getEntityList().get(v.argmax());
            } else {
                this.name = "#VALUE?";
            }
        }
        return this.name + " " + DocumentFactory.PIVOT_SUFFIX;
    }

    @Override
    public PivotValueHeader clone() {
        return new PivotValueHeader(this);
    }

    public Tensor1D buildEntityVector() {
        final Tensor1D result = new Tensor1D(ClassifierFactory.get().getLayoutClassifier().get().getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(),
            ClassifierFactory.get().getLayoutClassifier().get().getSampleCount()); i++) {
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
