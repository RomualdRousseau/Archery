package com.github.romualdrousseau.archery.parser.table;

import java.util.List;

import com.github.romualdrousseau.archery.base.BaseCell;
import com.github.romualdrousseau.archery.parser.layex.TableParser;

public abstract class DataTableParser extends TableParser<BaseCell> {

    public abstract List<Integer> getSplitRows();

    public abstract List<Integer> getIgnoreRows();
}
