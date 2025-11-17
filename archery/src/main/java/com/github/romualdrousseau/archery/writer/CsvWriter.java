package com.github.romualdrousseau.archery.writer;

import java.io.IOException;

import com.github.romualdrousseau.archery.base.BaseTable;

public class CsvWriter {

    private final BaseTable table;

    public CsvWriter(final BaseTable table) {
        this.table = table;
    }

    public void write(final String outputFilePath) throws IOException {
    }
}
