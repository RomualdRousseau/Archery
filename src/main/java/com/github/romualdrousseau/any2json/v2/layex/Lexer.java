package com.github.romualdrousseau.any2json.v2.layex;

public interface Lexer<S extends Symbol, C> {

    S read();

    S peek();

    void push();

    C pop();

    void seek(C i);
  }
