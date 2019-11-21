
package com.github.romualdrousseau.any2json.v2.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.IStream;
import com.github.romualdrousseau.any2json.v2.layex.ISymbol;

public class Or implements LayexMatcher {

    public Or(LinkedList<LayexMatcher> stack) {
        this.a = stack.pop();
        this.b = stack.pop();
    }

    @Override
    public <S extends ISymbol, C> boolean match(IStream<S, C> stream, Context<S> context) {
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

    private LayexMatcher a;
    private LayexMatcher b;
}
