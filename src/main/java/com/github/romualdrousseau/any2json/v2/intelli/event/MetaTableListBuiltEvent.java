package com.github.romualdrousseau.any2json.v2.intelli.event;

import java.util.List;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.intelli.MetaTable;

public class MetaTableListBuiltEvent extends SheetEvent {

    public MetaTableListBuiltEvent(Sheet source, List<MetaTable> tables) {
        super(source);
        this.MetaTables = tables;
    }

    public List<MetaTable> getMetaTables() {
        return this.MetaTables;
    }

    private List<MetaTable> MetaTables;
}
