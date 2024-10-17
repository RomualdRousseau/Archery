package com.github.romualdrousseau.archery.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial8 implements Runnable {

    public static void main(final String[] args) {
        new Tutorial8().run();
    }

    @Override
    public void run() {
        final var builder = Common.loadModelBuilderFromGitHub("sales-english");

        final var model = builder
                .setTableParser(this.customTableParser())
                .build();

        final var file = Common.loadData("AG120-N-074.pdf", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printHeaders(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    private LayexTableParser customTableParser() {
        return new LayexTableParser(
                List.of(""),
                List.of(
                        "((vv$)(v+$v+$))(()(.+$)())+()",
                        "(()(.+$))(()(.+$)())+()"));
    }
}
