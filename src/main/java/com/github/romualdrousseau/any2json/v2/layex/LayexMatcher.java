package com.github.romualdrousseau.any2json.v2.layex;

public interface LayexMatcher {

    <S extends ISymbol, C> boolean match(IStream<S, C> s, Context<S> c);
}
