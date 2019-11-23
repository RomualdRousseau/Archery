package com.github.romualdrousseau.any2json.v2.intelli.event;

import com.github.romualdrousseau.any2json.v2.Sheet;
import com.github.romualdrousseau.any2json.v2.SheetEvent;
import com.github.romualdrousseau.shuju.cv.ISearchBitmap;

public class BitmapGeneratedEvent extends SheetEvent {

    public BitmapGeneratedEvent(Sheet source, ISearchBitmap bitmap) {
        super(source);
        this.bitmap = bitmap;
    }

    public ISearchBitmap getBitmap() {
        return this.bitmap;
    }

    private ISearchBitmap bitmap;
}
