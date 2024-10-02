package com.github.romualdrousseau.archery.event;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;
import com.github.romualdrousseau.archery.parser.sheet.SheetBitmap;

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
