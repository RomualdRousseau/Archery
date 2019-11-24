package com.github.romualdrousseau.any2json.v2.intelli.event;

import java.util.List;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.base.AbstractTable;

public class AllTablesExtractedEvent extends SheetEvent {

    public AllTablesExtractedEvent(final Sheet source, final List<AbstractTable> tables) {
        super(source);
        this.tables = tables;
    }

    public List<AbstractTable> getTables() {
        return this.tables;
    }

    private final List<AbstractTable> tables;
}
