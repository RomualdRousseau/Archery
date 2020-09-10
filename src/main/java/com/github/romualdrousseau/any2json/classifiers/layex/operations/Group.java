
package com.github.romualdrousseau.any2json.classifiers.layex.operations;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.base.Context;
import com.github.romualdrousseau.any2json.base.Symbol;
import com.github.romualdrousseau.any2json.base.TableMatcher;
import com.github.romualdrousseau.any2json.classifiers.layex.Lexer;

public class Group implements TableMatcher {

    public Group(LinkedList<TableMatcher> stack, int group) {
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
