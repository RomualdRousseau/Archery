
package com.github.romualdrousseau.archery.parser.layex.operations;

import java.util.Deque;

import com.github.romualdrousseau.archery.base.Symbol;
import com.github.romualdrousseau.archery.parser.layex.Lexer;
import com.github.romualdrousseau.archery.parser.layex.TableMatcher;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public class Group implements TableMatcher {

    public Group(final Deque<TableMatcher> stack, final int group) {
      this.a = stack.pop();
      this.group = group;
    }

    @Override
    public <S extends Symbol, C> boolean match(final Lexer<S, C> stream, final TableParser<S> context) {
        if(context != null) {
            context.setGroup(this.group);
        }
        return omatch(stream, context, this.a);
    }

    @Override
    public String toString() {
      return "GROUP(" + this.a + ")";
    }

    private static final <S extends Symbol, C> boolean omatch(final Lexer<S, C> stream, final TableParser<S> context, final TableMatcher a) {
        return a instanceof Nop || a.match(stream, context);
    }

    private final TableMatcher a;
    private final int group;
  }
