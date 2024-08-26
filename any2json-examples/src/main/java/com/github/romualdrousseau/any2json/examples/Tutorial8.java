package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial8 implements Runnable {

    public Tutorial8() {
    }

    @Override
    public void run() {
        final var tableParser = new LayexTableParser(
                List.of(""),
                List.of(
                    "((vv$)(v+$v+$))(()(.+$)())+()",
                    "(()(.+$))(()(.+$)())+()"));

        final var builder = Common.loadModelBuilderFromGitHub("sales-english");
        builder.setTableParser(tableParser);
        final var model = builder.build();

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

    public static void main(final String[] args) {
        new Tutorial8().run();
    }
}
