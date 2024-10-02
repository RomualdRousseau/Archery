
package com.github.romualdrousseau.archery.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.layex.TableParser;
import com.github.romualdrousseau.archery.layex.Lexer;
import com.github.romualdrousseau.archery.layex.TableMatcher;

public class Many implements TableMatcher {

    public Many(final Deque<TableMatcher> stack, final int minCount, final int maxCount) {
        this.a = stack.pop();
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        int count = 0;
        while (true) {
            stream.push();
            if ((count >= maxCount) || !omatch(stream, context, a)) {
                stream.seek(stream.pop());
                return (minCount <= count);
            } else {
                count++;
                stream.pop();
            }
        }
    }

    @Override
    public String toString() {
        return "MANY(" + this.a + ", " + this.minCount + ", " + this.maxCount + ")";
    }

    private static final <S extends Symbol, C> boolean omatch(final Lexer<S, C> stream, final TableParser<S> context, final TableMatcher a) {
        return a instanceof Nop || a.match(stream, context);
    }

    private final TableMatcher a;
    private final int minCount;
    private final int maxCount;
}
