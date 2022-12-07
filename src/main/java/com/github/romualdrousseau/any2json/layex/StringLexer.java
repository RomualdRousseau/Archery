package com.github.romualdrousseau.any2json.layex;

import java.util.LinkedList;

public class StringLexer implements Lexer<StringSymbol, Integer> {

    public StringLexer(final String s) {
        this.stack = new LinkedList<Integer>();
        this.s = s;
        this.i = 0;
    }

    public StringSymbol read() {
        if (this.i >= this.s.length()) {
            return StringSymbol.EndOfStream;
        } else {
            return new StringSymbol(this.s.charAt(this.i++));
        }
    }

    public StringSymbol peek() {
        if (this.i >= this.s.length()) {
            return StringSymbol.EndOfStream;
        } else {
            return new StringSymbol(this.s.charAt(this.i));
        }
    }

    public void push() {
        this.stack.push(this.i);
    }

    public Integer pop() {
        return this.stack.pop();
    }

    public void seek(final Integer i) {
      this.i = i;
    }

    LinkedList<Integer> stack;
    String s;
    int i;
  }
