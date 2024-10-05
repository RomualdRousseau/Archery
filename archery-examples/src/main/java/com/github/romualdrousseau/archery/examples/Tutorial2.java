package com.github.romualdrousseau.archery.examples;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial2 implements Runnable {

    public static void main(final String[] args) {
        new Tutorial2().run();
    }

    @Override
    public void run() {
        final var builder = Common.loadModelBuilderFromGitHub("sales-english");

        final var model = builder
                .setTableParser(this.customTableParser())
                .setEntityList(this.customEntities(builder.getEntityList()))
                .setPatternMap(this.customPatternMap(builder.getPatternMap()))
                .build();

        final var file = Common.loadData("document with multiple tables.xlsx", this.getClass());
        try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
                .setModel(model)
                .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))
                .setRecipe("sheet.setCapillarityThreshold(0)")) {
            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
                Common.printHeaders(t.headers());
                Common.printRows(t.rows());
            }));
        }
    }

    private LayexTableParser customTableParser() {
        return new LayexTableParser(
                List.of("(v.$)+"),
                List.of("(()(S+$))(()([/^TOTAL/|v].+$)())+(/TOTAL/.+$)"));
    }

    private List<String> customEntities(final List<String> entities) {
        final var result = new ArrayList<String>(entities);
        result.add("PRODUCTNAME");
        result.remove("PACKAGE");
        return result;
    }

    private Map<String, String> customPatternMap(final Map<String, String> patterns) {
        final var result = new HashMap<String, String>(patterns);
        result.put("\\D+\\dml", "PRODUCTNAME");
        return result;
    }
}
