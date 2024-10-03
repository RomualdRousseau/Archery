package com.github.romualdrousseau.archery.parser.layex;

import java.util.ArrayDeque;
import java.util.Deque;

public class StringLexer implements Lexer<StringSymbol, Integer> {

    public StringLexer(final String s) {
        this.stack = new ArrayDeque<>();
        this.s = s;
        this.i = 0;
    }

    @Override
    public StringLexer reset() {
        this.stack.clear();
        this.i = 0;
        return this;
    }

    @Override
    public StringSymbol read() {
        if (this.i >= this.s.length()) {
            return StringSymbol.EndOfStream;
        } else {
            return new StringSymbol(this.s.charAt(this.i++));
        }
    }

    @Override
    public StringSymbol peek() {
        if (this.i >= this.s.length()) {
            return StringSymbol.EndOfStream;
        } else {
            return new StringSymbol(this.s.charAt(this.i));
        }
    }

    @Override
    public void push() {
        this.stack.push(this.i);
    }

    @Override
    public Integer pop() {
        return this.stack.pop();
    }

    @Override
    public void seek(final Integer i) {
        this.i = i;
    }

    private final Deque<Integer> stack;
    private final String s;
    private int i;

}
