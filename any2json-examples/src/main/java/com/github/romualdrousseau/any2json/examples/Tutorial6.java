package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial6 implements Runnable {

    public Tutorial6() {
    }

    @Override
    public void run() {
        final var tableParser = new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(E+$E+$))(()(/^PRODUCTCODE/.+$)*(/PRODUCTCODE/.+$))+()"));

        final var builder = Common.loadModelBuilderFromGitHub("sales-english");
        builder.setTableParser(tableParser);
        final var model = builder.build();

        final var file = Common.loadData("document with noises.xls", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))
                .setRecipe(
                    "sheet.setCapillarityThreshold(1.5)",
                        "sheet.setDataTableParserFactory(\"DataTableGroupSubFooterParserFactory\")",
                        "sheet.dropRowsWhenFillRatioLessThan(0.2)")) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printHeaders(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    public static void main(final String[] args) {
        new Tutorial6().run();
    }
}
