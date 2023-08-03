package com.github.romualdrousseau.any2json.loader.excel.xlsx;

import java.util.ArrayList;

public class XlsxRow {

    public static final float DEFAULT_HEIGHT = 15.0f * 4.0f / 3.0f;

    public XlsxRow() {
        this.height = XlsxRow.DEFAULT_HEIGHT;
        this.lastColumnNum = -1;
        this.isNotIgnorable = false;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getLastColumnNum() {
        return this.lastColumnNum;
    }

    public void setLastColumnNum(int lastColumnNum) {
        this.lastColumnNum = lastColumnNum;
    }

    public boolean isNotIgnorable() {
        return this.isNotIgnorable;
    }

    public void setNotIgnorable(boolean isNotIgnorable) {
        this.isNotIgnorable = isNotIgnorable;
    }

    public ArrayList<XlsxCell> cells() {
        return this.cells;
    }

    public void addCell(XlsxCell cell) {
        if(this.cells == null) {
            this.cells = new ArrayList<XlsxCell>();
        }
        this.cells.add(cell);
    }

    private float height;
    private int lastColumnNum;
    private boolean isNotIgnorable;
    private ArrayList<XlsxCell> cells;
}
