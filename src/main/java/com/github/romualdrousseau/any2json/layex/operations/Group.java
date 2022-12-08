
package com.github.romualdrousseau.any2json.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.layex.Lexer;
import com.github.romualdrousseau.any2json.layex.Symbol;
import com.github.romualdrousseau.any2json.layex.TableMatcher;

public class Group implements TableMatcher {

    public Group(Deque<TableMatcher> stack, int group) {
      this.a = stack.pop();
      this.group = group;
    }

    @Override
    public <S extends Symbol, C> boolean match(Lexer<S, C> stream, Context<S> context) {
        if(context != null) {
            context.setGroup(this.group);
        }
        return this.a.match(stream, context);
    }

    @Override
    public String toString() {
      return "GROUP(" + this.a + ")";
    }

    private TableMatcher a;
    private int group;
  }
