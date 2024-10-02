package com.github.romualdrousseau.archery.intelli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.config.Settings;
import com.github.romualdrousseau.archery.header.DataTableHeader;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;

public class IntelliHeader extends DataTableHeader {

    public IntelliHeader(final BaseHeader header, final boolean disableAutoName) {
        super(header.getTable(), new BaseCell(header.getName(), header.getColumnIndex(), 1,
                header.getCell().getRawValue(), header.getTable().getSheet()));

        final String cellValue = this.getCell().getValue();
        if (StringUtils.isFastBlank(cellValue)) {
            if (header.isColumnEmpty()) {
                this.name = "";
            } else {
                this.name = String.format(this.getTable().getSheet().getColumnValueFormat(),
                        StreamSupport.stream(this.entities().spliterator(), false).findAny()
                                .map(x -> this.getEntitiesAsString()).orElse("VALUE"));
            }
        } else if (this.isPivotHeader() || !disableAutoName) {
            this.name = this.getTable().getSheet().getDocument().getModel().toEntityName(cellValue);
        } else {
            this.name = cellValue;
        }

        this.disableAutoName = disableAutoName;

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

        final var value = IntelliHeader.mergeValues(values);
        return value
                .map(x -> new BaseCell(x, this.getColumnIndex(), 1, this.getTable().getSheet()))
                .orElseGet(() -> this.getCellAtRow(row));
    }

    @Override
    public BaseHeader clone() {
        return new IntelliHeader(this, this.disableAutoName);
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
    private final boolean disableAutoName;
    private IntelliHeader prevSibling;
    private IntelliHeader nextSibling;
}
