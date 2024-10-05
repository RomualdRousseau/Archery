package com.github.romualdrousseau.archery.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial7 implements Runnable {

    public static void main(final String[] args) {
        new Tutorial7().run();
    }

    @Override
    public void run() {
        final var builder = Common.loadModelBuilderFromGitHub("sales-english");

        final var model = builder
                .setTableParser(this.customTableParser())
                .build();

        final var file = Common.loadData("document with noises.pdf", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))
                .setRecipe("""
                        sheet.dropRowsWhenFillRatioLessThan(0.5)
                        sheet.cropWhenFillRatioLessThan(0.5)
                        pos = sheet.searchNthValue("MONTH", 0, 2, 1)
                        if pos is not None:
                            col, row = pos
                            sheet.patchCells(col, row, col, row, ["MONTH UNIT", "YEAR UNIT"])
                        pos = sheet.searchNthValue("MONTH", 0, 2, 1)
                        if pos is not None:
                            col, row = pos
                            sheet.patchCells(col, row, col, row, ["MONTH DOLLAR", "YEAR DOLLAR"])
                        """)) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printHeaders(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    private LayexTableParser customTableParser() {
        return new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(.+$.+$))(()([/PACKAGE/].+$)+([/^PACKAGE/|E].+$){1,2})+([/^PACKAGE/|E].+$)"));
    }
}
