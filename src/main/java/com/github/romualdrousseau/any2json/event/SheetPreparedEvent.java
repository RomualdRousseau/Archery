package com.github.romualdrousseau.any2json.event;

import com.github.romualdrousseau.any2json.Sheet;
import com.github.romualdrousseau.any2json.SheetEvent;

public class SheetPreparedEvent extends SheetEvent {

    public SheetPreparedEvent(Sheet source) {
        super(source);
    }    
}
