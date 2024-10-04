package com.github.romualdrousseau.archery.event;

import java.util.List;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;
import com.github.romualdrousseau.archery.base.MetaTable;

public class MetaTableListBuiltEvent extends SheetEvent {

    public MetaTableListBuiltEvent(final Sheet source, final List<MetaTable> tables) {
        super(source);
        this.MetaTables = tables;
    }

    public List<MetaTable> getMetaTables() {
        return this.MetaTables;
    }

    private final List<MetaTable> MetaTables;
}
