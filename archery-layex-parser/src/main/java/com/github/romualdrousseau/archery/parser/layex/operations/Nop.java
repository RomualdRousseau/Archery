package com.github.romualdrousseau.archery.parser.layex.operations;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.parser.layex.Layex;
import com.github.romualdrousseau.archery.parser.layex.Lexer;
import com.github.romualdrousseau.archery.parser.layex.TableMatcher;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public class Nop implements TableMatcher {

    public Nop(final Layex layex) {
        this.layex = layex;
    }

    @Override
    public Layex getLayex() {
        return this.layex;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        return true;
    }

    @Override
    public String toString() {
        return "NOP";
    }

    private final Layex layex;
}
