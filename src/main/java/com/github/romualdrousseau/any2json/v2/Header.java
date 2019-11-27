package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.shuju.DataRow;

public interface Header {

    String getName();

    String getValue();

    Cell getCellAtRow(Row row);

    Cell getCellAtRow(Row row, boolean merged);

    boolean hasTag();

    HeaderTag getTag();

    String getEntityString();

    DataRow buildTrainingRow(final String tagValue, final Header[] conflicts,
            final boolean ensureWordsExists);
}
