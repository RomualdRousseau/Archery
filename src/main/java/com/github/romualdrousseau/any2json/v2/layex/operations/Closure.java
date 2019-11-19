
package com.github.romualdrousseau.any2json.v2.layex.operations;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.IStream;
import com.github.romualdrousseau.any2json.v2.layex.ISymbol;

public class Closure implements LayexMatcher {

    public Closure(ArrayList<LayexMatcher> stack, int minCount, int maxCount) {
        this.a = stack.remove(stack.size() - 1);
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public <S extends ISymbol, C> boolean match(IStream<S, C> stream, Context<S> context) {
        int count = 0;
        while (true) {
            stream.push();
            if (!this.a.match(stream, context) || (count > maxCount)) {
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
