package com.github.romualdrousseau.any2json.v2.loader.excel.xlsx;

import java.util.ArrayList;

public class XlsxRow {

    public static final float DEFAULT_HEIGHT = 15.0f * 4.0f / 3.0f;

    public float height;
    public int lastColumnNum;
    public boolean isNotIgnorable;
    public ArrayList<XlsxCell> cells;

    public XlsxRow() {
        this.height = XlsxRow.DEFAULT_HEIGHT;
        this.lastColumnNum = -1;
        this.isNotIgnorable = false;
    }

    public void addCell(XlsxCell cell) {
        if(this.cells == null) {
            this.cells = new ArrayList<XlsxCell>();
        }
        this.cells.add(cell);
    }
}
