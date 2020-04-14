package com.github.romualdrousseau.any2json.layex.operations;

import com.github.romualdrousseau.any2json.layex.Context;
import com.github.romualdrousseau.any2json.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;

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
