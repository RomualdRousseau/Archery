package com.github.romualdrousseau.any2json.classifiers.layex;

import com.github.romualdrousseau.any2json.base.Symbol;

public class StringSymbol implements Symbol {

    public static final StringSymbol EndOfStream = new StringSymbol('$');

    public StringSymbol(final char c) {
        this.s = String.valueOf(c);
    }

    @Override
    public String getSymbol() {
        if (this == StringSymbol.EndOfStream) {
            return "";
        } else {
            return this.s;
        }
    }

    private final String s;
}
