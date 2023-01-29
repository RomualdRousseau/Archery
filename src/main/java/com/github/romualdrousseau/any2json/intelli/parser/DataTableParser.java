package com.github.romualdrousseau.any2json.intelli.parser;

import java.util.List;

import com.github.romualdrousseau.any2json.base.BaseCell;
import com.github.romualdrousseau.any2json.layex.TableParser;

public abstract class DataTableParser extends TableParser<BaseCell> {
    public abstract List<Integer> getSplitRows();

    public abstract List<Integer> getIgnoreRows();
}
