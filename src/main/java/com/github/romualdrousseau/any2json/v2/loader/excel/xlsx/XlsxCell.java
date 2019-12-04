package com.github.romualdrousseau.any2json.v2.loader.excel.xlsx;

public class XlsxCell {

    public static final XlsxCell Empty = new XlsxCell();

    public String value;
    public int length;

    public XlsxCell() {
        this.value = null;
        this.length = 1;
    }
}
