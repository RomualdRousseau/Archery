package com.github.romualdrousseau.archery.header;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.base.BaseHeader;
import com.github.romualdrousseau.archery.base.BaseTable;

public class PivotKeyHeader extends MetaHeader {

    private final List<PivotEntry> entries;
    private final String pivotEntityName;

    public PivotKeyHeader(final BaseTable table, final BaseCell cell) {
        this(table, cell, cell.getPivotKeyEntityAsString().get(), null);
    }

    protected PivotKeyHeader(final BaseTable table, final BaseCell cell, final String pivotEntityName,
            final List<PivotEntry> entries) {
        super(table, cell);
        this.pivotEntityName = pivotEntityName;
        this.entries = (entries == null)
                ? new ArrayList<>(List.of(new PivotEntry(cell, pivotEntityName)))
                : entries;
    }

    @Override
    public BaseHeader clone() {
        return new PivotKeyHeader(this.getTable(), this.getCell(), this.pivotEntityName, this.entries);
    }

    @Override
    public String getName() {
        return String.format(this.getTable().getSheet().getPivotKeyFormat(), super.getName());
    }

    public String getPivotEntityName() {
        return this.pivotEntityName;
    }

    public List<PivotEntry> getEntries() {
        return this.entries;
    }

    public void addEntry(final BaseCell entry) {
        this.entries.add(new PivotEntry(entry, this.pivotEntityName));
    }

    public Set<String> getEntryTypeValues() {
        return this.entries.stream().map(x -> x.getTypeValue()).collect(Collectors.toSet());
    }

    public void setEntryTypeValues(final Set<String> entryTypeValues) {
        this.entries.clear();
        this.entries.addAll(entryTypeValues.stream()
                .map(x -> new PivotEntry(this.getCell(), this.pivotEntityName).setTypeValue(x)).toList());
    }

    public Set<String> getEntryPivotValues() {
        return this.entries.stream().map(x -> x.getPivotValue()).collect(Collectors.toSet());
    }

    public PivotTypeHeader getPivotTypeHeader() {
        return new PivotTypeHeader(this, this.pivotEntityName);
    }

    public PivotValueHeader getPivotValueHeader() {
        return new PivotValueHeader(this, this.pivotEntityName);
    }
}
