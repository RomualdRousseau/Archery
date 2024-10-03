package com.github.romualdrousseau.archery.parser.layex;

import com.github.romualdrousseau.archery.base.Symbol;

public abstract class TableParser<S extends Symbol> {

    public int getGroup() {
        return this.group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getColumn() {
        return this.column;
    }

    public int getRow() {
        return this.row;
    }

    public void notify(S s) {
        this.processSymbolFunc(s);

        this.column++;

        if (s.getSymbol().equals("$")) {
            this.column = 0;
            this.row++;
        }
    }

    public abstract void processSymbolFunc(S s);

    private int group = 0;
    private int column = 0;
    private int row = 0;
}
