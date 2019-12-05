package com.github.romualdrousseau.any2json.v2.loader.excel.xlsb;

public class XlsbCell {

    public static final XlsbCell Empty = new XlsbCell();

    public String value;
    public int length;

    public XlsbCell() {
        this.value = null;
        this.length = 1;
    }
}
