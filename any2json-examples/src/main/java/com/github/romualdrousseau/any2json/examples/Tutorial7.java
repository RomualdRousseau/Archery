package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial7 implements Runnable {

    public Tutorial7() {
    }

    @Override
    public void run() {
        final var tableParser = new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(v.+$v.+$))(()(e.+$)+())(v.+$)"));

        final var builder = Common.loadModelBuilderFromGitHub("sales-english");
        builder.setTableParser(tableParser);
        final var model = builder.build();

        final var file = Common.loadData("document with noises.pdf", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))
                .setRecipe(
                    "sheet.setCapillarityThreshold(0)",
                    "sheet.dropNullRows(0.45)")) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printHeaders(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    public static void main(final String[] args) {
        new Tutorial7().run();
    }
}
