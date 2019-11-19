package com.github.romualdrousseau.any2json.v2.layex;

import java.util.ArrayList;

import com.github.romualdrousseau.any2json.v2.layex.operations.Closure;
import com.github.romualdrousseau.any2json.v2.layex.operations.Concat;
import com.github.romualdrousseau.any2json.v2.layex.operations.Group;
import com.github.romualdrousseau.any2json.v2.layex.operations.Nop;
import com.github.romualdrousseau.any2json.v2.layex.operations.Or;
import com.github.romualdrousseau.any2json.v2.layex.operations.Value;

public class Layex {
    public Layex(String pattern) {
        this.pattern = new StringStream(pattern);
    }

    public LayexMatcher compile() {
        this.stack = new ArrayList<LayexMatcher>();
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
                this.stack.add(e2);
                this.stack.add(e1);
                return new Concat(this.stack);
            }
        }
    }

    private LayexMatcher r2() {
        String c = this.getSymbol();

        if (c.charAt(0) >= 'a' && c.charAt(0) <= 'z' || c.charAt(0) == '$') {
            this.acceptPreviousSymbol();
            LayexMatcher e = new Value(c);
            return r3(e);
        } else if (c.equals("(")) {
            this.acceptPreviousSymbol();
            LayexMatcher e = r();
            this.acceptSymbol(")");
            this.stack.add(r3(e));
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
            this.stack.add(e);
            return new Closure(this.stack, 0, 1);
        } else if (c.equals("*")) {
            this.acceptPreviousSymbol();
            this.stack.add(e);
            return new Closure(this.stack, 0, Integer.MAX_VALUE);
        } else if (c.equals("+")) {
            this.acceptPreviousSymbol();
            this.stack.add(e);
            return new Closure(this.stack, 1, Integer.MAX_VALUE);
        } else if (c.equals("{")) {
            this.acceptPreviousSymbol();
            LayexMatcher e2 = r4(e);
            this.acceptSymbol("}");
            return e2;
        } else if (c.equals("|")) {
            this.acceptPreviousSymbol();
            LayexMatcher e2 = r();
            this.stack.add(e2);
            this.stack.add(e);
            return new Or(this.stack);
        } else {
            return e;
        }
    }

    private LayexMatcher r4(LayexMatcher e) {
        String c = this.getSymbol();

        if (c.charAt(0) >= '0' && c.charAt(0) <= '9') {
            this.acceptPreviousSymbol();
            int n = Integer.valueOf(c);
            this.stack.add(e);
            return new Closure(this.stack, n, n);
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

    private ArrayList<LayexMatcher> stack;
    private StringStream pattern;
    private int groupCounter;
}
