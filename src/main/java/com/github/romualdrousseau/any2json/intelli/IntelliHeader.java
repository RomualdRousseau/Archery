package com.github.romualdrousseau.any2json.intelli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.romualdrousseau.any2json.Row;
import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.base.BaseHeader;
import com.github.romualdrousseau.any2json.config.Settings;
import com.github.romualdrousseau.any2json.header.DataTableHeader;
import com.github.romualdrousseau.shuju.strings.StringUtils;

public class IntelliHeader extends DataTableHeader {

    public IntelliHeader(final BaseHeader header) {
        super(header.getTable(), new BaseCell(header.getName(), header.getColumnIndex(), 1,
                header.getCell().getRawValue(), header.getTable().getSheet()));

        final String cellValue = this.getCell().getValue();
        if (StringUtils.isFastBlank(cellValue)) {
            if (header.isColumnEmpty()) {
                this.name = "";
            } else {
                this.name = this.entities().stream().findAny().map(x -> this.getEntitiesAsString())
                        .orElse(Settings.PIVOT_VALUE_SUFFIX);
            }
        } else {
            this.name = this.getTable().getSheet().getDocument().getModel().toEntityName(cellValue);
        }

        this.setColumnIndex(header.getColumnIndex());
        this.setColumnEmpty(StringUtils.isFastBlank(this.name) && header.isColumnEmpty());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public BaseCell getCellAtRow(final Row row, final boolean merged) {
        if (!merged || this.nextSibling == null) {
            return this.getCellAtRow(row);
        }

        // Rewind to the beginning of the chain

        IntelliHeader curr = this;
        while (curr.prevSibling != null) {
            curr = curr.prevSibling;
        }

        // Concat each element of the chain

        final var values = new ArrayList<String>();
        while (curr != null) {
            values.add(curr.getCellAtRow(row).getValue());
            curr = curr.nextSibling;
        }

        return IntelliHeader.mergeValues(values)
                .map(x -> new BaseCell(x, this.getColumnIndex(), 1, this.getTable().getSheet()))
                .orElseGet(() -> this.getCellAtRow(row));
    }

    @Override
    public BaseHeader clone() {
        return new IntelliHeader(this);
    }

    @Override
    public void resetTag() {
        super.resetTag();
        this.prevSibling = null;
        this.nextSibling = null;
    }

    @Override
    public boolean isColumnMerged() {
        return this.prevSibling != null;
    }

    public void mergeTo(final IntelliHeader other) {
        IntelliHeader e = this;
        while (e.nextSibling != null) {
            e = e.nextSibling;
        }
        other.prevSibling = e;
        e.nextSibling = other;
    }

    public static Optional<String> mergeValues(final List<String> values) {
        return StringUtils.merge(Settings.MERGE_SEPARATOR, values);
    }

    private final String name;
    private IntelliHeader prevSibling;
    private IntelliHeader nextSibling;
}
