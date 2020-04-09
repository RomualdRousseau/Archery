package com.github.romualdrousseau.any2json.layex;

public interface LayexMatcher {

    <S extends Symbol, C> boolean match(Lexer<S, C> s, Context<S> c);
}
