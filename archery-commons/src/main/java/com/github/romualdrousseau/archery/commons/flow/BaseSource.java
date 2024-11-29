package com.github.romualdrousseau.archery.commons.flow;

import java.util.Objects;
import java.util.Optional;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public abstract class BaseSource<C, R> implements Publisher<C, R>, Runnable {

    private final Broker<C, R> broker = new Broker<>();
    private long countOfProcessedRows = 0;
    private LoggerWithLabels logger;

    @Override
    public BaseSource<C, R> downstream(final Subscriber<C, R> subscriber) {
        this.broker.addSuscriber(subscriber);
        return this;
    }

    @Override
    public long getCountOfProcessedRows() {
        return this.countOfProcessedRows;
    }

    @Override
    public LoggerWithLabels getLogger() {
        return Objects.requireNonNull(Optional.ofNullable(this.logger).orElseGet(this::getDefaultLogger));
    }

    public BaseSource<C, R> setLogger(final LoggerWithLabels logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public void publishStart(final C context) {
        this.countOfProcessedRows = 0;
        this.broker.forwardSetLogger(this.getLogger());
        this.broker.forwardStart(context);
    }

    @Override
    public void publishRow(final C context, final R row) {
        this.broker.forwardRow(context, row);
        this.countOfProcessedRows++;
    }

    @Override
    public void publishEnd(final C context) {
        this.broker.forwardEnd(context);
    }

    protected abstract LoggerWithLabels getDefaultLogger();
}
