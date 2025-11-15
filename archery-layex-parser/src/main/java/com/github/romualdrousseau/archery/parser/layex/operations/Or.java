
package com.github.romualdrousseau.archery.parser.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.parser.layex.Layex;
import com.github.romualdrousseau.archery.parser.layex.Lexer;
import com.github.romualdrousseau.archery.parser.layex.TableMatcher;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public class Or implements TableMatcher {

    public Or(final Layex layex, final Deque<TableMatcher> stack) {
        this.layex = layex;
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public Layex getLayex() {
        return this.layex;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        stream.push();
        if (omatch(stream, context, a)) {
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

    private static final <S extends Symbol, C> boolean omatch(final Lexer<S, C> stream, final TableParser<S> context,
            final TableMatcher a) {
        return a instanceof Nop || a.match(stream, context);
    }

    private final Layex layex;
    private final TableMatcher a;
    private final TableMatcher b;
}
