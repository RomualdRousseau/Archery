package com.github.romualdrousseau.any2json.loader.excel.xlsx;

public class XlsxCell {

    public static final XlsxCell Empty = new XlsxCell();

    public XlsxCell() {
        this.decorated = false;
        this.value = null;
    }

    public boolean isDecorated() {
        return this.decorated;
    }

    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public XlsxCell copy() {
        XlsxCell newCell = new XlsxCell();
        newCell.value = this.value;
        newCell.decorated = this.decorated;
        return newCell;
    }

    private String value;
    private boolean decorated;
}
