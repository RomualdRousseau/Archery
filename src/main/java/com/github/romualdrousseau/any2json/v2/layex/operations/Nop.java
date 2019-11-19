package com.github.romualdrousseau.any2json.v2.layex.operations;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.IStream;
import com.github.romualdrousseau.any2json.v2.layex.ISymbol;

public class Nop implements LayexMatcher {
    public Nop() {
    }

    @Override
    public <S extends ISymbol, C> boolean match(IStream<S, C> stream, Context<S> context) {
        return true;
    }

    @Override
    public String toString() {
        return "nop";
    }
  }
