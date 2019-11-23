package com.github.romualdrousseau.any2json.v2.layex;

public class StringSymbol implements Symbol {

    public static final StringSymbol EndOfStream = new StringSymbol('$');

    public StringSymbol(char c) {
        this.s = String.valueOf(c);
    }

	@Override
    public String getSymbol() {
        if(this == StringSymbol.EndOfStream) {
            return "";
        } else {
            return this.s;
        }
    }

    private String s;
}
