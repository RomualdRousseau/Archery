package com.github.romualdrousseau.any2json.v2;

public abstract class SheetEvent {

    public SheetEvent(Sheet source) {
        this.source = source;
    }

    public Sheet getSource() {
        return this.source;
    }

    private Sheet source;
}
