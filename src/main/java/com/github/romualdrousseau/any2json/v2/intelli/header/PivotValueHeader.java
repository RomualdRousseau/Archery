package com.github.romualdrousseau.any2json.v2.intelli.header;

import com.github.romualdrousseau.any2json.v2.DocumentFactory;
import com.github.romualdrousseau.any2json.v2.base.BaseCell;
import com.github.romualdrousseau.any2json.v2.base.BaseRow;
import com.github.romualdrousseau.shuju.math.Vector;

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
            final Vector v = this.getEntityVector();
            if(v.sparsity() < 1.0f) {
                this.name = this.getTable().getClassifier().getEntityList().get(v.argmax());
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

    @Override
    public Vector buildEntityVector() {
        final Vector result = new Vector(this.getTable().getClassifier().getEntityList().getVectorSize());

        int n = 0;
        for (int i = 0; i < Math.min(this.getTable().getNumberOfRows(),
                this.getTable().getClassifier().getSampleCount()); i++) {
            final BaseRow row = (BaseRow) this.getTable().getRowAt(i);
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

    private String name;
}
