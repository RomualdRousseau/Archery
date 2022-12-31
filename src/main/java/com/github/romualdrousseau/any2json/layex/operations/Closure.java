
package com.github.romualdrousseau.any2json.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.any2json.layex.TableParser;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Closure implements TableMatcher {

    public Closure(Deque<TableMatcher> stack, int minCount, int maxCount) {
        this.a = stack.pop();
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, TableParser<S> context) {
        int count = 0;
        while (true) {
            stream.push();
            if ((count >= maxCount) || !this.a.match(stream, context)) {
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
        return "CLOSURE(" + this.a + ", " + this.minCount + ", " + this.maxCount + ")";
    }

    private TableMatcher a;
    private int minCount;
    private int maxCount;
}
