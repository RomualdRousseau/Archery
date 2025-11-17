package com.github.romualdrousseau.archery.parser.layex;

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.romualdrousseau.archery.parser.layex.operations.Any;
import com.github.romualdrousseau.archery.parser.layex.operations.Concat;
import com.github.romualdrousseau.archery.parser.layex.operations.EndOfRow;
import com.github.romualdrousseau.archery.parser.layex.operations.Group;
import com.github.romualdrousseau.archery.parser.layex.operations.Literal;
import com.github.romualdrousseau.archery.parser.layex.operations.LiteralNeg;
import com.github.romualdrousseau.archery.parser.layex.operations.Many;
import com.github.romualdrousseau.archery.parser.layex.operations.Nop;
import com.github.romualdrousseau.archery.parser.layex.operations.Or;
import com.github.romualdrousseau.archery.parser.layex.operations.Value;
import com.github.romualdrousseau.archery.parser.layex.operations.ValueNeg;

public class Layex {
    public Layex(final String layex) {
        this.layex = layex;
        this.pattern = new StringLexer(layex.replaceAll(" ", ""));
    }

    public TableMatcher compile() {
        this.stack = new ArrayDeque<TableMatcher>();
        this.groupCounter = 0;
        return this.r();
    }

    public String toString() {
        return this.layex;
    }

    private TableMatcher r() {
        // Grammar
        // R = RR
        // R = R|R
        // R = (R)
        // R = [R]
        // R = R?
        // R = R*
        // R = R+
        // R = R$
        // R = R{n,m}|R{n,}
        // R = /lit/
        // R = /^lit/
        // R = s|v|e|.
        final String c = this.getSymbol();

        if (c.equals("")) {
            return new Nop(this);
        } else if (c.equals(")")) {
            return new Nop(this);
        } else if (c.equals("]")) {
            return new Nop(this);
        } else {
            final TableMatcher e1 = r2();
            final TableMatcher e2 = r();
            if (e2 instanceof Nop) { // Small optimization
                return e1;
            } else {
                this.stack.push(e2);
                this.stack.push(e1);
                return new Concat(this, this.stack);
            }
        }
    }

    private TableMatcher r2() {
        final String c = this.getSymbol();
        if (c.charAt(0) >= 'a' && c.charAt(0) <= 'z') {
            this.acceptSymbol();
            return r3(new Value(this, c));
        } else if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z') {
            this.acceptSymbol();
            return r3(new ValueNeg(this, c));
        } else if (c.charAt(0) == '.') {
            this.acceptSymbol();
            return r3(new Any());
        } else if (c.charAt(0) == '$') {
            this.acceptSymbol();
            return r3(new EndOfRow(this));
        } else if (c.equals("(")) {
            this.acceptSymbol();
            final int group = this.groupCounter++;
            final TableMatcher e = r();
            this.acceptSymbol(")");
            this.stack.push(r3(e));
            return new Group(this, this.stack, group);
        } else if (c.equals("[")) {
            this.acceptSymbol();
            final TableMatcher e = r();
            this.acceptSymbol("]");
            return r3(e);
        } else if (c.equals("/")) {
            this.acceptSymbol();
            final TableMatcher e = this.lit();
            this.acceptSymbol("/");
            return r3(e);
        } else {
            throw new RuntimeException("Syntax Error: " + c);
        }
    }

    private TableMatcher r3(final TableMatcher e) {
        final String c = this.getSymbol();
        if (c.equals("?")) {
            this.acceptSymbol();
            this.stack.push(e);
            return new Many(this, this.stack, 0, 1);
        } else if (c.equals("*")) {
            this.acceptSymbol();
            this.stack.push(e);
            return new Many(this, this.stack, 0, Integer.MAX_VALUE);
        } else if (c.equals("+")) {
            this.acceptSymbol();
            this.stack.push(e);
            return new Many(this, this.stack, 1, Integer.MAX_VALUE);
        } else if (c.equals("{")) {
            this.acceptSymbol();
            final TableMatcher e2 = r4(e);
            this.acceptSymbol("}");
            return e2;
        } else if (c.equals("|")) {
            this.acceptSymbol();
            final TableMatcher e2 = r();
            this.stack.push(e2);
            this.stack.push(e);
            return new Or(this, this.stack);
        } else {
            return e;
        }
    }

    private TableMatcher r4(final TableMatcher e) {
        String c = this.getSymbol();
        if (c.charAt(0) >= '0' && c.charAt(0) <= '9') {
            this.acceptSymbol();
            final int n1 = Integer.valueOf(c);
            int n2 = n1;
            c = this.getSymbol();
            if (c.equals(",")) {
                this.acceptSymbol();
                n2 = Integer.MAX_VALUE;
                c = this.getSymbol();
                if (c.charAt(0) >= '0' && c.charAt(0) <= '9') {
                    this.acceptSymbol();
                    n2 = Integer.valueOf(c);
                }
            }
            this.stack.push(e);
            return new Many(this, this.stack, n1, n2);
        } else {
            throw new RuntimeException("Syntax Error: " + c);
        }
    }

    private TableMatcher lit() {
        String l = "";
        boolean neg = false;
        String c = this.getSymbol();
        if (c.equals("^")) {
            this.acceptSymbol();
            neg = true;
            c = this.getSymbol();
        }
        while (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z' || c.charAt(0) >= 'a' && c.charAt(0) <= 'z') {
            this.acceptSymbol();
            l += c;
            c = this.getSymbol();
        }
        return neg ? new LiteralNeg(this, l) : new Literal(l);
    }

    private String getSymbol() {
        return this.pattern.peek().getSymbol();
    }

    private void acceptSymbol() {
        this.pattern.read();
    }

    private void acceptSymbol(final String s) {
        if (!this.pattern.read().getSymbol().equals(s)) {
            throw new RuntimeException("Syntax Error");
        }
    }

    private final String layex;
    private final StringLexer pattern;

    private Deque<TableMatcher> stack;
    private int groupCounter;
}
