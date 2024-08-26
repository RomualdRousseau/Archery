package com.github.romualdrousseau.any2json.examples;

import java.util.List;

import com.github.romualdrousseau.any2json.DocumentFactory;

public class Tutorial1 implements Runnable {

    private static List<String> FILES = List.of(
            "document with simple table.csv",
            "document with simple table.xls",
            "document with simple table.xlsx");

    public Tutorial1() {
    }

    @Override
    public void run() {
        FILES.forEach(f -> {
            final var file = Common.loadData(f, this.getClass());
            try (final var doc = DocumentFactory.createInstance(file, "UTF-8")) {
                doc.sheets().forEach(s -> s.getTable().ifPresent(t -> {
                    Common.printHeaders(t.headers());
                    Common.printRows(t.rows());
                }));
            }
        });
    }

    public static void main(final String[] args) {
        new Tutorial1().run();
    }
}
