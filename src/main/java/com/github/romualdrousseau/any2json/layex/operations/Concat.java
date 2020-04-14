package com.github.romualdrousseau.any2json.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.layex.Context;
import com.github.romualdrousseau.any2json.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;

public class Concat implements LayexMatcher {

    public Concat(LinkedList<LayexMatcher> stack) {
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        if(!this.a.match(stream, context)) {
            return false;
        } else {
            return this.b.match(stream, context);
        }
    }

    @Override
    public String toString() {
        return "CONCAT(" + this.a + "," + this.b + ")";
    }

    private LayexMatcher a;
    private LayexMatcher b;
}
