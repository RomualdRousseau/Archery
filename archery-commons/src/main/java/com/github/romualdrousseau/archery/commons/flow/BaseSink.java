package com.github.romualdrousseau.archery.commons.flow;

import java.util.Objects;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public abstract class BaseSink<C, R> implements Subscriber<C, R>{

    private long countOfProcessedRows = 0;
    private LoggerWithLabels logger;

    @Override
    public long getCountOfProcessedRows() {
        return this.countOfProcessedRows;
    }

    @Override
    public LoggerWithLabels getLogger() {
        return Objects.requireNonNull(this.logger);
    }

    @Override
    public Subscriber<C, R> setLogger(final LoggerWithLabels logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public void initialize(final C context) {
        this.countOfProcessedRows = 0;
    }

    @Override
    public void accept(final C context, final R record) {
        this.countOfProcessedRows++;
    }

    @Override
    public void finalize(final C context) {
    }
}
