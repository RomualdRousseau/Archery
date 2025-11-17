package com.github.romualdrousseau.archery;

import java.io.Closeable;
import java.io.IOException;

public interface Table extends Closeable {

    Sheet getSheet();

    int getNumberOfColumns();

    int getNumberOfRows();

    Row getRowAt(final int rowIndex);

    Iterable<Row> rows();

    int getNumberOfHeaders();

    Iterable<String> getHeaderNames();

    Header getHeaderAt(final int i);

    Iterable<Header> headers();

    int getNumberOfHeaderTags();

    Iterable<Header> headerTags();

    void updateHeaderTags();

    void to_arrow(final String outputfilePath) throws IOException;

    void to_csv(final String outputFilePath) throws IOException;

    void to_json(final String outputFilePath) throws IOException;
}
