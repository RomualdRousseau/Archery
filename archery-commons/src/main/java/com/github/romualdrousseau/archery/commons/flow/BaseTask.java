package com.github.romualdrousseau.archery.commons.flow;

import java.util.Objects;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public abstract class BaseTask<C, I, O> implements Publisher<C, O>, Subscriber<C, I> {

    private final Broker<C, O> broker = new Broker<>();
    private long countOfProcessedRows = 0;
    private LoggerWithLabels logger;

    @Override
    public BaseTask<C, I, O> downstream(final Subscriber<C, O> subscriber) {
        this.broker.addSuscriber(subscriber);
        return this;
    }

    @Override
    public long getCountOfProcessedRows() {
        return this.countOfProcessedRows;
    }

    @Override
    public LoggerWithLabels getLogger() {
        return Objects.requireNonNull(this.logger);
    }

    @Override
    public Subscriber<C, I> setLogger(final LoggerWithLabels logger) {
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
    public void publishRow(final C context, final O row) {
        this.broker.forwardRow(context, row);
        this.countOfProcessedRows++;
    }

    @Override
    public void publishEnd(final C context) {
        this.broker.forwardEnd(context);
    }

    @Override
    public void initialize(final C context) {
        this.publishStart(context);
    }

    @Override
    public void finalize(final C context) {
        this.publishEnd(context);
    }
}
