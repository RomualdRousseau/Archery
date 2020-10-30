package com.github.romualdrousseau.any2json;

import com.github.romualdrousseau.shuju.DataRow;

public interface Header {

    String getRawName();

    String getName();

    Cell getCellAtRow(Row row);

    Cell getCellAtRow(Row row, boolean merged);

    boolean hasTag();

    HeaderTag getTag();

    String getEntityString();

    DataRow buildTrainingRow(final String tagValue, final boolean ensureWordsExists);
}
