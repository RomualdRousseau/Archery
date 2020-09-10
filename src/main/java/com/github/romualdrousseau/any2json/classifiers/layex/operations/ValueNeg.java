package com.github.romualdrousseau.any2json.classifiers.layex.operations;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public class ValueNeg implements TableMatcher {

    public ValueNeg(String v) {
        this.v = v.toLowerCase();
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        S symbol = stream.read();
        String c = symbol.getSymbol();
        if (!c.equals("") && c.charAt(0) >= 'a' && c.charAt(0) <= 'z' && !c.equals(this.v)) {
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
        return "!VALUE('" + this.v + "')";
    }

    private String v;
}
