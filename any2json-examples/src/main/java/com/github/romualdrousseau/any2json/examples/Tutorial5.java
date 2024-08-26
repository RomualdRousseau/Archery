package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial5 implements Runnable {

    public Tutorial5() {
    }

    @Override
    public void run() {
        final var tableParser = new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(S+$S+$))(()([/^TOTAL/|v].+$)())+(/TOTAL/.+$)"));

        final var builder = Common.loadModelBuilderFromGitHub("sales-english");
        builder.setTableParser(tableParser);
        builder.getEntityList().add("PRODUCTNAME");
        builder.getPatternMap().put("\\D+\\dml", "PRODUCTNAME");
        builder.getPatternMap().put("(?i)((20|19)\\d{2}-(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)-\\d{2})", "DATE");
        final var model = builder.build();

        final var file = Common.loadData("document with pivot.xlsx", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT, Document.Hint.INTELLI_TAG))
                .setRecipe(
                        "sheet.setCapillarityThreshold(0)",
                        "sheet.setPivotOption(\"WITH_TYPE_AND_VALUE\")",
                        "sheet.setPivotTypeFormat(\"%s\")")) {

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printTags(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    public static void main(final String[] args) {
        new Tutorial5().run();
    }
}
