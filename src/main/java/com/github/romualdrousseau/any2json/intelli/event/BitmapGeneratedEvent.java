package com.github.romualdrousseau.any2json.intelli.event;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;
import com.github.romualdrousseau.any2json.base.SheetBitmap;

public class BitmapGeneratedEvent extends SheetEvent {

    public BitmapGeneratedEvent(final Sheet source, final SheetBitmap bitmap) {
        super(source);
        this.bitmap = bitmap;
    }

    public SheetBitmap getBitmap() {
        return this.bitmap;
    }

    private final SheetBitmap bitmap;
}
