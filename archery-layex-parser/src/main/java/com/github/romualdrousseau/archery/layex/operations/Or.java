
package com.github.romualdrousseau.archery.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.layex.TableParser;
import com.github.romualdrousseau.archery.layex.Lexer;
import com.github.romualdrousseau.archery.layex.TableMatcher;

public class Or implements TableMatcher {

    public Or(final Deque<TableMatcher> stack) {
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        stream.push();
        if(omatch(stream, context, a)) {
            stream.pop();
            return true;
        } else {
            stream.seek(stream.pop());
            return omatch(stream, context, b);
        }
    }

    @Override
    public String toString() {
        return "OR(" + this.a + "," + this.b + ")";
    }

    private static final <S extends Symbol, C> boolean omatch(final Lexer<S, C> stream, final TableParser<S> context, final TableMatcher a) {
        return a instanceof Nop || a.match(stream, context);
    }

    private final TableMatcher a;
    private final TableMatcher b;
}
