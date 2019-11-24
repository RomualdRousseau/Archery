package com.github.romualdrousseau.any2json.v2.intelli.event;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.any2json.v2.base.SheetBitmap;

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
