package com.github.romualdrousseau.archery.commons.flow;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public interface Publisher<C, R> {

    Publisher<C, R> downstream(Subscriber<C, R> subscriber);

    long getCountOfProcessedRows();

    LoggerWithLabels getLogger();

    void publishStart(C context);

    void publishRow(C context, R row);

    void publishEnd(C context);
}
