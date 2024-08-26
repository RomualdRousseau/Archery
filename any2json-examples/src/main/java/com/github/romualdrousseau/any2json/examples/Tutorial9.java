package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.TableGraph;
import com.github.romualdrousseau.any2json.base.DataTable;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial9 implements Runnable {

    public Tutorial9() {
    }

    public void visitTable(TableGraph parent) {
        parent.children().forEach(c -> {
            final var table = c.getTable();
            if (table instanceof DataTable) {
                Common.printHeaders(table.headers());
                Common.printRows(table.rows());
            }
            if (c.children().size() > 0) {
                this.visitTable(c);
            }
        });
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

            doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTableGraph().ifPresent(this::visitTable));
        }
    }

    public static void main(final String[] args) {
        new Tutorial9().run();
    }
}
