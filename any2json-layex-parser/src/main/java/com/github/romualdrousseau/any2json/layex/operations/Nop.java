package com.github.romualdrousseau.any2json.layex.operations;

import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.layex.TableParser;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Nop implements TableMatcher {

    public Nop() {
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        return true;
    }

    @Override
    public String toString() {
        return "NOP";
    }
  }
