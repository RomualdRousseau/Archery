package com.github.romualdrousseau.any2json.classifiers.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public class Concat implements TableMatcher {

    public Concat(LinkedList<TableMatcher> stack) {
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

    private TableMatcher a;
    private TableMatcher b;
}
