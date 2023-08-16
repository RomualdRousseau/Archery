
package com.github.romualdrousseau.any2json.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.layex.TableParser;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Or implements TableMatcher {

    public Or(Deque<TableMatcher> stack) {
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, TableParser<S> context) {
        stream.push();
        if(this.a.match(stream, context)) {
            stream.pop();
            return true;
        } else {
            stream.seek(stream.pop());
            return this.b.match(stream, context);
        }
    }

    @Override
    public String toString() {
        return "OR(" + this.a + "," + this.b + ")";
    }

    private TableMatcher a;
    private TableMatcher b;
}
