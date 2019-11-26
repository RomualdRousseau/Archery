
package com.github.romualdrousseau.any2json.v2.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.Lexer;
import com.github.romualdrousseau.any2json.v2.layex.Symbol;

public class Closure implements LayexMatcher {

    public Closure(LinkedList<LayexMatcher> stack, int minCount, int maxCount) {
        this.a = stack.pop();
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
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

    private LayexMatcher a;
    private int minCount;
    private int maxCount;
}
