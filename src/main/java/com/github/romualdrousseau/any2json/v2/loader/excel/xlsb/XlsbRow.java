package com.github.romualdrousseau.any2json.v2.loader.excel.xlsb;

import java.util.ArrayList;

public class XlsbRow {

    public static final float DEFAULT_HEIGHT = 15.0f * 4.0f / 3.0f;

    public float height;
    public int lastColumnNum;
    public boolean isNotIgnorable;
    public ArrayList<XlsbCell> cells;

    public XlsbRow() {
        this.height = XlsbRow.DEFAULT_HEIGHT;
        this.lastColumnNum = -1;
        this.isNotIgnorable = false;
    }

    public void addCell(XlsbCell cell) {
        if(this.cells == null) {
            this.cells = new ArrayList<XlsbCell>();
        }
        this.cells.add(cell);
    }
}
