package com.github.romualdrousseau.any2json.v2.layex;

import java.util.LinkedList;

import com.github.romualdrousseau.any2json.v2.layex.operations.Any;
import com.github.romualdrousseau.any2json.v2.layex.operations.Closure;
import com.github.romualdrousseau.any2json.v2.layex.operations.Concat;
import com.github.romualdrousseau.any2json.v2.layex.operations.EndOfRow;
import com.github.romualdrousseau.any2json.v2.layex.operations.Group;
import com.github.romualdrousseau.any2json.v2.layex.operations.Nop;
import com.github.romualdrousseau.any2json.v2.layex.operations.Or;
import com.github.romualdrousseau.any2json.v2.layex.operations.Value;
import com.github.romualdrousseau.any2json.v2.layex.operations.ValueNeg;

public class Layex {
    public Layex(String pattern) {
        this.pattern = new StringLexer(pattern.replaceAll(" ", ""));
    }

    public LayexMatcher compile() {
        this.stack = new LinkedList<LayexMatcher>();
        this.groupCounter = 0;
        return this.r();
    }

    private LayexMatcher r() {
        // Grammar
        // R = RR
        // R = R|R
        // R = (R)
        // R = [R]
        // R = R?
        // R = R*
        // R = R+
        // R = R{n}
        // R = s
        String c = this.getSymbol();

        if (c.equals("")) {
            return new Nop();
        } else if (c.equals(")")) {
            return new Nop();
        } else if (c.equals("]")) {
            return new Nop();
        } else {
            LayexMatcher e1 = r2();
            LayexMatcher e2 = r();

            if (e2 instanceof Nop) { // Small optimization
                return e1;
            } else {
                this.stack.push(e2);
                this.stack.push(e1);
                return new Concat(this.stack);
            }
        }
    }

    private LayexMatcher r2() {
        String c = this.getSymbol();

        if (c.charAt(0) >= 'a' && c.charAt(0) <= 'z') {
            this.acceptPreviousSymbol();
            return r3(new Value(c));
        } else if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z') {
            this.acceptPreviousSymbol();
            return r3(new ValueNeg(c));
        } else if (c.charAt(0) == '.') {
            this.acceptPreviousSymbol();
            return r3(new Any());
        } else if (c.charAt(0) == '$') {
            this.acceptPreviousSymbol();
            return r3(new EndOfRow());
        } else if (c.equals("(")) {
            this.acceptPreviousSymbol();
            LayexMatcher e = r();
            this.acceptSymbol(")");
            this.stack.push(r3(e));
            return new Group(this.stack, this.groupCounter++);
        } else if (c.equals("[")) {
            this.acceptPreviousSymbol();
            LayexMatcher e = r();
            this.acceptSymbol("]");
            return r3(e);
        } else {
            throw new RuntimeException("Syntax Error: " + c);
        }
    }

    private LayexMatcher r3(LayexMatcher e) {
        String c = this.getSymbol();

        if (c.equals("?")) {
            this.acceptPreviousSymbol();
            this.stack.push(e);
            return new Closure(this.stack, 0, 1);
        } else if (c.equals("*")) {
            this.acceptPreviousSymbol();
            this.stack.push(e);
            return new Closure(this.stack, 0, Integer.MAX_VALUE);
        } else if (c.equals("+")) {
            this.acceptPreviousSymbol();
            this.stack.push(e);
            return new Closure(this.stack, 1, Integer.MAX_VALUE);
        } else if (c.equals("{")) {
            this.acceptPreviousSymbol();
            LayexMatcher e2 = r4(e);
            this.acceptSymbol("}");
            return e2;
        } else if (c.equals("|")) {
            this.acceptPreviousSymbol();
            LayexMatcher e2 = r();
            this.stack.push(e2);
            this.stack.push(e);
            return new Or(this.stack);
        } else {
            return e;
        }
    }

    private LayexMatcher r4(LayexMatcher e) {
        String c = this.getSymbol();

        if (c.charAt(0) >= '0' && c.charAt(0) <= '9') {
            this.acceptPreviousSymbol();
            int n1 = Integer.valueOf(c);
            int n2 = n1;

            c = this.getSymbol();
            if(c.equals(",")) {
                this.acceptPreviousSymbol();
                n2 = Integer.MAX_VALUE;

                c = this.getSymbol();
                if (c.charAt(0) >= '0' && c.charAt(0) <= '9') {
                    this.acceptPreviousSymbol();
                    n2 = Integer.valueOf(c);
                }
            }

            this.stack.push(e);
            return new Closure(this.stack, n1, n2);
        } else {
            throw new RuntimeException("Syntax Error: " + c);
        }
    }

    private String getSymbol() {
        return this.pattern.peek().getSymbol();
    }

    private void acceptPreviousSymbol() {
        this.pattern.read();
    }

    private void acceptSymbol(String s) {
        if (!this.pattern.read().getSymbol().equals(s)) {
            throw new RuntimeException("Syntax Error");
        }
    }

    private LinkedList<LayexMatcher> stack;
    private StringLexer pattern;
    private int groupCounter;
}
