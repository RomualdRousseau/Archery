package com.github.romualdrousseau.any2json.classifiers.layex.operations;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public class Nop implements TableMatcher {

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
