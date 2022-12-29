package com.github.romualdrousseau.any2json.layex;

public interface TableMatcher {
    <S extends Symbol, C> boolean match(Lexer<S, C> s, TableContext<S> c);
}
