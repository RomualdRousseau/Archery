package com.github.romualdrousseau.any2json.layex;

import com.github.romualdrousseau.any2json.base.Symbol;

public interface TableMatcher {

    default <S extends Symbol, C> boolean match(Lexer<S, C> s) {
        return match(s, null);
    }

    <S extends Symbol, C> boolean match(Lexer<S, C> s, TableParser<S> c);
}
