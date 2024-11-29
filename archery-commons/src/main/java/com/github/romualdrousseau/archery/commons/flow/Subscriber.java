package com.github.romualdrousseau.archery.commons.flow;

import java.util.function.BiConsumer;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public interface Subscriber<C, R> extends BiConsumer<C, R> {

    long getCountOfProcessedRows();

    LoggerWithLabels getLogger();

    Subscriber<C, R> setLogger(final LoggerWithLabels logger);

    void initialize(C context);

    void finalize(C context);
}
