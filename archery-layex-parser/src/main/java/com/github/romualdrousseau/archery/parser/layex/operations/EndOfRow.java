package com.github.romualdrousseau.archery.parser.layex.operations;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.parser.layex.Layex;
import com.github.romualdrousseau.archery.parser.layex.Lexer;
import com.github.romualdrousseau.archery.parser.layex.TableMatcher;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public class EndOfRow implements TableMatcher {

    public EndOfRow(final Layex layex) {
        this.layex = layex;
    }

    @Override
    public Layex getLayex() {
        return this.layex;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        final var symbol = stream.read();
        final var c = symbol.getSymbol();
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

    private final Layex layex;
}
