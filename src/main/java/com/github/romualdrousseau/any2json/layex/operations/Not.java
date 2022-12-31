package com.github.romualdrousseau.any2json.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.layex.TableParser;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Not implements TableMatcher {

    public Not(LinkedList<TableMatcher> stack) {
        this.a = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, TableParser<S> context) {
        return !this.a.match(stream, context);
    }

    @Override
    public String toString() {
        return "NOT(" + this.a + ")";
    }

    private TableMatcher a;
}
