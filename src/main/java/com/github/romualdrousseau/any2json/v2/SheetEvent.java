package com.github.romualdrousseau.any2json.v2;

public abstract class SheetEvent {

    public SheetEvent(Sheet source) {
        this.source = source;
        this.canceled = false;
    }

    public Sheet getSource() {
        return this.source;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCancel(boolean flag) {
        this.canceled = flag;
    }

    private Sheet source;
    private boolean canceled;
}
