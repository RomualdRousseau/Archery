package com.github.romualdrousseau.any2json.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.layex.Context;
import com.github.romualdrousseau.any2json.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;

public class Not implements LayexMatcher {

    public Not(LinkedList<LayexMatcher> stack) {
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

    private LayexMatcher a;
}
