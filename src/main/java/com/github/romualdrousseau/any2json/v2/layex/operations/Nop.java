package com.github.romualdrousseau.any2json.v2.layex.operations;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.Lexer;
import com.github.romualdrousseau.any2json.v2.layex.Symbol;

public class Nop implements LayexMatcher {
    public Nop() {
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        return true;
    }

    @Override
    public String toString() {
        return "NOP";
    }
  }
