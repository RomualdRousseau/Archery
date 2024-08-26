package com.github.romualdrousseau.any2json.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.layex.TableParser;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Not implements TableMatcher {

    public Not(final LinkedList<TableMatcher> stack) {
        this.a = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        return !omatch(stream, context, a);
    }

    @Override
    public String toString() {
        return "NOT(" + this.a + ")";
    }

    private static final <S extends Symbol, C> boolean omatch(final Lexer<S, C> stream, final TableParser<S> context, final TableMatcher a) {
        return a instanceof Nop || a.match(stream, context);
    }

    private final TableMatcher a;
}
