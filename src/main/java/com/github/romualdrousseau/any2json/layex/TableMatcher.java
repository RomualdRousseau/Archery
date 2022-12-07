package com.github.romualdrousseau.any2json.layex;

import com.github.romualdrousseau.any2json.base.Context;

public interface TableMatcher {

    <S extends Symbol, C> boolean match(Lexer<S, C> s, Context<S> c);
}
