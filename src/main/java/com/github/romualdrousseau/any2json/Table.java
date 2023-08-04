package com.github.romualdrousseau.any2json;

public interface Table
{
    int getNumberOfColumns();

    int getNumberOfRows();

    Iterable<Row> rows();

    int getNumberOfHeaders();

    Iterable<String> getHeaderNames();

    Iterable<Header> headers();

    int getNumberOfHeaderTags();

    Iterable<Header> headerTags();

    void updateHeaderTags();
}
