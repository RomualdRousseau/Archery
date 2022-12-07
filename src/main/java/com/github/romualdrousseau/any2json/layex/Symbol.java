package com.github.romualdrousseau.any2json.layex;

public interface Symbol {

    String getSymbol();

    boolean matchLiteral(String literal);
}
