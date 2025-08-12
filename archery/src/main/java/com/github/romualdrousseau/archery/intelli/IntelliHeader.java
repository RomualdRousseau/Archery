package com.github.romualdrousseau.archery.intelli;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.archery.Row;
import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.commons.strings.StringUtils;
import com.github.romualdrousseau.archery.config.Settings;
import com.github.romualdrousseau.archery.header.DataTableHeader;

public class IntelliHeader extends DataTableHeader {

    private static Optional<String> mergeValues(final List<String> values) {
        return StringUtils.merge(Settings.MERGE_SEPARATOR, values);
    }

    private static BaseCell createCell(final BaseHeader header) {
        return new BaseCell(header.getName(), header.getColumnIndex(), 1,
                header.getCell().getRawValue(), header.getTable().getSheet());
    }

    private final String name;
    private final boolean disableAutoName;
    private IntelliHeader prevSibling;

    private IntelliHeader nextSibling;

    public IntelliHeader(final BaseHeader header, final boolean disableAutoName) {
        super(header.getTable(), createCell(header));
        this.name = this.determineHeaderName(header, disableAutoName);
        this.disableAutoName = disableAutoName;
        this.setColumnIndex(header.getColumnIndex());
        this.setColumnEmpty(isColumnEmpty(header));
    }

    protected IntelliHeader(final IntelliHeader parent) {
        this(parent, parent.disableAutoName);
    }

    @Override
    public BaseHeader clone() {
        return new IntelliHeader(this);
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
        return this.getMergedCellValue(row);
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
        var e = this;
        while (e.nextSibling != null) {
            e = e.nextSibling;
        }
        other.prevSibling = e;
        e.nextSibling = other;
    }

    private String determineHeaderName(final BaseHeader header, final boolean disableAutoName) {
        final var cellValue = this.getCell().getValue();
        if (StringUtils.isFastBlank(cellValue)) {
            return this.handleBlankCellValue(header);
        } else if (this.isPivotKeyHeader() || !disableAutoName) {
            return this.getTable().getSheet().getDocument().getModel().toEntityName(cellValue);
        } else {
            return cellValue;
        }
    }

    private String handleBlankCellValue(final BaseHeader header) {
        if (header.isColumnEmpty()) {
            return "";
        } else {
            return String.format(this.getTable().getSheet().getColumnValueFormat(),
                    StreamSupport.stream(this.entities().spliterator(), false).findAny()
                            .map(x -> this.getEntitiesAsString()).orElse("VALUE"));
        }
    }

    private boolean isColumnEmpty(final BaseHeader header) {
        return StringUtils.isFastBlank(this.name) && header.isColumnEmpty();
    }

    private BaseCell getMergedCellValue(final Row row) {
        final var values = this.collectValuesFromChain(row);
        final var value = IntelliHeader.mergeValues(values);
        return value
                .map(x -> new BaseCell(x, this.getColumnIndex(), 1, this.getTable().getSheet()))
                .orElseGet(() -> this.getCellAtRow(row));
    }

    private List<String> collectValuesFromChain(final Row row) {
        final var values = new ArrayList<String>();
        var curr = this;
        while (curr.prevSibling != null) {
            curr = curr.prevSibling;
        }
        while (curr != null) {
            values.add(curr.getCellAtRow(row).getValue());
            curr = curr.nextSibling;
        }
        return values;
    }
}
