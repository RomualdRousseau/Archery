package com.github.romualdrousseau.any2json.layex;

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
