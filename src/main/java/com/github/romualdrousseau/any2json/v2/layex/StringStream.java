package com.github.romualdrousseau.any2json.v2.layex;

import java.util.ArrayList;

public class StringStream implements IStream<StringSymbol, Integer> {

    public StringStream(String s) {
      this.stack = new ArrayList<Integer>();
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
      this.stack.add(this.i);
    }

    public Integer pop() {
      return this.stack.remove(this.stack.size() - 1);
    }

    public void seek(Integer i) {
      this.i = i;
    }

    ArrayList<Integer> stack;
    String s;
    int i;
  }
