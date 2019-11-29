package com.github.romualdrousseau.any2json.v2.loader.excel;

import java.io.Serializable;

import java.util.ArrayList;

public class MappedRow implements Serializable {
    private static final long serialVersionUID = -4587965466851661002L;
    public int lastColumnNum;
    public ArrayList<MappedCell> cells = new ArrayList<MappedCell>();
}
