package com.github.romualdrousseau.any2json.v2.loader.excel.xlsx;

public class XlsxCell {

    public static final XlsxCell Empty = new XlsxCell();

    public XlsxCell() {
        this.value = null;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String value;
}
