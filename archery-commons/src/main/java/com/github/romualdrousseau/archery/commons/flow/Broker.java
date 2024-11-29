package com.github.romualdrousseau.archery.commons.flow;

import java.util.ArrayList;

import com.github.romualdrousseau.archery.commons.logging.LoggerWithLabels;

public class Broker<C, R> {

    private ArrayList<Subscriber<C, R>> subscribers = new ArrayList<>();

    public void addSuscriber(Subscriber<C, R> subscriber) {
        this.subscribers.add(subscriber);
    }

    public void forwardSetLogger(final LoggerWithLabels logger) {
        this.subscribers.forEach(subscriber -> subscriber.setLogger(logger));
    }

    public void forwardStart(C context) {
        this.subscribers.forEach(subscriber -> subscriber.initialize(context));
    }

    public void forwardRow(C context, R row) {
        this.subscribers.forEach(subscriber -> subscriber.accept(context, row));
    }

    public void forwardEnd(C context) {
        this.subscribers.forEach(subscriber -> subscriber.finalize(context));
    }
}
