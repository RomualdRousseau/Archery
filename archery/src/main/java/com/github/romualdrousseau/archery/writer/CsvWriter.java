package com.github.romualdrousseau.archery.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.github.romualdrousseau.archery.Cell;
import com.github.romualdrousseau.archery.Header;
import com.github.romualdrousseau.archery.base.BaseTable;

public class CsvWriter {

    private final BaseTable table;

    public CsvWriter(final BaseTable table) {
        this.table = table;
    }

    public void write(final String outputFilePath) throws IOException {
        try (var writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            final var headers = new ArrayList<Header>();
            this.table.headers().forEach(headers::add);
            final var headerNames = headers.stream()
                    .map(h -> h.hasTag() ? h.getTag().getValue() : h.getName())
                    // .map(this::escapeCsv)
                    .collect(Collectors.joining(","));
            writer.write(headerNames);
            writer.newLine();

            for (final var row : this.table.rows()) {
                final var cells = new ArrayList<Cell>();
                row.cells().forEach(cells::add);
                final var values = cells.stream()
                        .map(c -> c.getValue())
                        .map(this::escapeCsv)
                        .collect(Collectors.joining(","));
                writer.write(values);
                writer.newLine();
            }
        }
    }

    private String escapeCsv(final String data) {
        if (data == null) {
            return "";
        }
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}
