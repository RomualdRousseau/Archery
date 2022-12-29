package com.github.romualdrousseau.any2json.layex.operations;

import com.github.romualdrousseau.any2json.layex.TableContext;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Nop implements TableMatcher {

    public Nop() {
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, TableContext<S> context) {
        return true;
    }

    @Override
    public String toString() {
        return "NOP";
    }
  }
