package com.github.romualdrousseau.any2json.v2.layex.operations;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.Lexer;
import com.github.romualdrousseau.any2json.v2.layex.Symbol;

public class EndOfRow implements LayexMatcher {

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        S symbol = stream.read();
        String c = symbol.getSymbol();
        if (!c.equals("") && c.equals("$")) {
            if (context != null) {
                context.notify(symbol);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "EOR";
    }
}
