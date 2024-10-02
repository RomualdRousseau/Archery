package com.github.romualdrousseau.archery.layex.operations;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.layex.TableParser;
import com.github.romualdrousseau.archery.layex.Lexer;
import com.github.romualdrousseau.archery.layex.TableMatcher;

public class Value implements TableMatcher {

    public Value(final String v) {
        this.v = v;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        final var symbol = stream.read();
        final var c = symbol.getSymbol();
        if (!c.equals("") && c.charAt(0) >= 'a' && c.charAt(0) <= 'z' && c.equals(this.v)) {
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
        return "VALUE('" + this.v + "')";
    }

    private final String v;
}
