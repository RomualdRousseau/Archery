package com.github.romualdrousseau.any2json.base;

import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public interface TableMatcher {

    <S extends Symbol, C> boolean match(Lexer<S, C> s, Context<S> c);
}
