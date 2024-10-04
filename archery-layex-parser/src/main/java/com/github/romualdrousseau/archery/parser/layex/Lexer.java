package com.github.romualdrousseau.archery.parser.layex;

import com.github.romualdrousseau.archery.base.Symbol;

public interface Lexer<S extends Symbol, C> {

    Lexer<S, C> reset();

    S read();

    S peek();

    void push();

    C pop();

    void seek(C i);
  }
