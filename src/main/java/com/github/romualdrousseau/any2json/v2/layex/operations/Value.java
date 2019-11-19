package com.github.romualdrousseau.any2json.v2.layex.operations;

import com.github.romualdrousseau.any2json.v2.layex.Context;
import com.github.romualdrousseau.any2json.v2.layex.LayexMatcher;
import com.github.romualdrousseau.any2json.v2.layex.IStream;
import com.github.romualdrousseau.any2json.v2.layex.ISymbol;

public class Value implements LayexMatcher {

    public Value(String v) {
      this.v = v;
    }

    @Override
    public <S extends ISymbol, C> boolean match(IStream<S, C> stream, Context<S> context) {
      S symbol = stream.read();
      String c = symbol.getSymbol();
      if (!c.equals("") && c.equals(this.v)) {
        if(context != null) {
            context.notify(symbol);
        }
        return true;
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return "value('" + this.v + "')";
    }

    private String v;
  }
