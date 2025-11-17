package com.github.romualdrousseau.archery.writer;

import java.io.IOException;

import com.github.romualdrousseau.archery.base.BaseTable;

public class JsonWriter {

    private final BaseTable table;

    public JsonWriter(final BaseTable table) {
        this.table = table;
    }

    public void write(String outputFilePath) throws IOException {
    }
}
