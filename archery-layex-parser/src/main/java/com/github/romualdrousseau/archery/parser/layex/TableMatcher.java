package com.github.romualdrousseau.archery.parser.layex;

import com.github.romualdrousseau.archery.base.Symbol;

public interface TableMatcher {

    default <S extends Symbol, C> boolean match(Lexer<S, C> s) {
        return match(s, null);
    }

    <S extends Symbol, C> boolean match(Lexer<S, C> s, TableParser<S> c);
}
