package com.github.romualdrousseau.any2json.v2;

import com.github.romualdrousseau.shuju.DataRow;
import com.github.romualdrousseau.shuju.math.Vector;

public interface Header {

    String getName();

    String getValue();

    boolean hasTag();

    HeaderTag getTag();

    Vector getEntityVector();

    DataRow buildTrainingRow(final String tagValue, final Header[] conflicts,
            final boolean ensureWordsExists);
}
