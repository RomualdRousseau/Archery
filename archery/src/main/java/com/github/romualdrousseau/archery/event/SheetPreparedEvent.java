package com.github.romualdrousseau.archery.event;

import com.github.romualdrousseau.archery.Sheet;
import com.github.romualdrousseau.archery.SheetEvent;

public class SheetPreparedEvent extends SheetEvent {

    public SheetPreparedEvent(Sheet source) {
        super(source);
    }    
}
