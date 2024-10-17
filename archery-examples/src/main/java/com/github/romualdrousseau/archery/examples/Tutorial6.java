package com.github.romualdrousseau.archery.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial6 implements Runnable {

    public static void main(final String[] args) {
        new Tutorial6().run();
    }

    @Override
    public void run() {
        final var builder = Common.loadModelBuilderFromGitHub("sales-english");

        final var model = builder
                .setTableParser(this.customTableParser())
                .build();

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

    private LayexTableParser customTableParser() {
        return new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(E+$E+$))(()(/^PRODUCTCODE/.+$)*(/PRODUCTCODE/.+$))+()"));
    }
}
