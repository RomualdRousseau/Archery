package com.github.romualdrousseau.archery.base;

public interface Symbol {

    String getSymbol();

    boolean matchLiteral(String literal);
}
