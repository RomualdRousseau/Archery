package com.github.romualdrousseau.any2json.v2;

public interface Table {

    int getNumberOfColumns();

    int getNumberOfRows();

    Iterable<Row> rows();

    int getNumberOfHeaders();

    Iterable<Header> headers();
}
