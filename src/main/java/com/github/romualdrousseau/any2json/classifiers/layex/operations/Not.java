package com.github.romualdrousseau.any2json.classifiers.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public class Not implements TableMatcher {

    public Not(LinkedList<TableMatcher> stack) {
        this.a = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        return !this.a.match(stream, context);
    }

    @Override
    public String toString() {
        return "NOT(" + this.a + ")";
    }

    private TableMatcher a;
}
