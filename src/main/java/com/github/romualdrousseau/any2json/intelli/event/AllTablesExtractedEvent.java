package com.github.romualdrousseau.any2json.intelli.event;

import java.util.List;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.intelli.CompositeTable;

public class AllTablesExtractedEvent extends SheetEvent {

    public AllTablesExtractedEvent(final Sheet source, final List<CompositeTable> tables) {
        super(source);
        this.tables = tables;
    }

    public List<CompositeTable> getTables() {
        return this.tables;
    }

    private final List<CompositeTable> tables;
}
