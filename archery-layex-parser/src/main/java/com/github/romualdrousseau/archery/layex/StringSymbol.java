package com.github.romualdrousseau.archery.layex;

import com.github.romualdrousseau.archery.base.Symbol;

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

    @Override
    public boolean matchLiteral(String literal) {
        return true;
    }

    private final String s;
}
