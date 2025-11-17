package com.github.romualdrousseau.archery.event;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;

public class SheetReadyEvent extends SheetEvent {

    public SheetReadyEvent(Sheet source) {
        super(source);
    }
}
