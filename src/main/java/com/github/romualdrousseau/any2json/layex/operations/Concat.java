package com.github.romualdrousseau.any2json.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.any2json.layex.TableContext;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Concat implements TableMatcher {

    public Concat(Deque<TableMatcher> stack) {
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, TableContext<S> context) {
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
